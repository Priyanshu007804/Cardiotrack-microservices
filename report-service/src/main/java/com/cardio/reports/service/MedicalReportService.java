package com.cardio.reports.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.cardio.reports.dto.MedicalReportDownloadDTO;
import com.cardio.reports.dto.MedicalReportResponseDTO;
import com.cardio.reports.dto.PatientProfileResponseDTO;
import com.cardio.reports.exception.ResourceNotFoundException;
import com.cardio.reports.model.MedicalReport;
import com.cardio.reports.model.MedicalReportStatus;
import com.cardio.reports.repository.MedicalReportRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class MedicalReportService {

    private static final Logger log = LoggerFactory.getLogger(MedicalReportService.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final Cloudinary cloudinary;
    private final MedicalReportRepository medicalReportRepository;
    private final MedicalReportDeletionService medicalReportDeletionService;
    private final RestClient patientServiceRestClient;

    public MedicalReportService(
            Cloudinary cloudinary,
            MedicalReportRepository medicalReportRepository,
            MedicalReportDeletionService medicalReportDeletionService,
            RestClient patientServiceRestClient) {
        this.cloudinary = cloudinary;
        this.medicalReportRepository = medicalReportRepository;
        this.medicalReportDeletionService = medicalReportDeletionService;
        this.patientServiceRestClient = patientServiceRestClient;
    }

    // ==========================================
    // File Signature (Magic Bytes) Validation
    // ==========================================
    private boolean hasValidFileSignature(byte[] fileBytes, String contentType) {
        if (fileBytes == null || fileBytes.length < 4) {
            return false;
        }

        // PDF signature: %PDF-
        if ("application/pdf".equals(contentType)) {
            return fileBytes.length >= 5
                    && fileBytes[0] == '%'
                    && fileBytes[1] == 'P'
                    && fileBytes[2] == 'D'
                    && fileBytes[3] == 'F'
                    && fileBytes[4] == '-';
        }

        // JPEG signature: FF D8 FF
        if ("image/jpeg".equals(contentType)) {
            return fileBytes.length >= 3
                    && (fileBytes[0] & 0xFF) == 0xFF
                    && (fileBytes[1] & 0xFF) == 0xD8
                    && (fileBytes[2] & 0xFF) == 0xFF;
        }

        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        if ("image/png".equals(contentType)) {
            byte[] signature = {
                    (byte) 0x89, 0x50, 0x4E, 0x47,
                    0x0D, 0x0A, 0x1A, 0x0A
            };

            if (fileBytes.length < signature.length) {
                return false;
            }

            for (int i = 0; i < signature.length; i++) {
                if (fileBytes[i] != signature[i]) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    // ==========================================
    // Downstream Patient Service Helpers
    // ==========================================
    private void validatePatientExists(Long patientId) {
        try {
            patientServiceRestClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient not found with id: " + patientId);
            }
            throw ex;
        }
    }

    private Long resolveCallerPatientId() {
        try {
            PatientProfileResponseDTO profile = patientServiceRestClient.get()
                    .uri("/api/patients/me/profile")
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);

            if (profile == null || profile.getId() == null) {
                throw new ResourceNotFoundException("Patient profile not found for authenticated user");
            }

            return profile.getId();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient profile not found for authenticated user");
            }
            throw ex;
        }
    }

    private void enforcePatientAccess(Long targetPatientId, String callerRole) {
        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!callerPatientId.equals(targetPatientId)) {
                throw new AccessDeniedException("You can only access your own medical reports");
            }
        }
    }

    // ==========================================
    // Upload Medical Report
    // ==========================================
    @Transactional
    public MedicalReportResponseDTO uploadMedicalReport(
            Long patientId,
            Long medicalTestId,
            MultipartFile file,
            Long callerUserId,
            String callerRole) throws IOException {

        // 1. Role check: Patients cannot upload reports
        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            throw new AccessDeniedException("You are not authorized to upload medical reports");
        }

        // 2. Validate file presence
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // 3. Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 10 MB");
        }

        // 4. Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF, JPEG, and PNG files are allowed");
        }

        // 5. Validate magic bytes signature
        byte[] fileBytes = file.getBytes();
        if (!hasValidFileSignature(fileBytes, contentType)) {
            throw new IllegalArgumentException("File content does not match the allowed file type");
        }

        // 6. Verify patient exists in Patient Service
        validatePatientExists(patientId);

        // 7. Generate unique storage key (UUID)
        String storageKey = UUID.randomUUID().toString();

        // 8. Upload to Cloudinary with authenticated raw settings
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                fileBytes,
                Map.of(
                        "public_id", storageKey,
                        "resource_type", "raw",
                        "type", "authenticated"
                )
        );

        // 9. Sanitize file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "medical-report";
        }
        originalFileName = Paths.get(originalFileName).getFileName().toString();

        // 10. Persist metadata to database
        MedicalReport report = new MedicalReport();
        report.setPatientId(patientId);
        report.setMedicalTestId(medicalTestId);
        report.setUploadedByUserId(callerUserId);
        report.setFileName(originalFileName);
        report.setContentType(contentType);
        report.setStorageKey((String) uploadResult.get("public_id"));
        report.setFileSizeBytes(file.getSize());
        report.setUploadedAt(LocalDateTime.now());
        report.setStatus(MedicalReportStatus.ACTIVE);

        MedicalReport savedReport;
        try {
            savedReport = medicalReportRepository.save(report);
            medicalReportRepository.flush();
        } catch (Exception e) {
            // Rollback Cloudinary upload if DB persistence fails
            log.error("Failed to save report to database, rolling back Cloudinary upload: {}", storageKey);
            try {
                cloudinary.uploader().destroy(
                        storageKey,
                        ObjectUtils.asMap(
                                "resource_type", "raw",
                                "type", "authenticated"
                        )
                );
            } catch (Exception cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }

        return mapToResponseDTO(savedReport);
    }

    // ==========================================
    // Get Reports By Patient ID
    // ==========================================
    @Transactional(readOnly = true)
    public List<MedicalReportResponseDTO> getReportsByPatientId(
            Long patientId,
            Long callerUserId,
            String callerRole) {

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            enforcePatientAccess(patientId, callerRole);
        } else {
            validatePatientExists(patientId);
        }

        return medicalReportRepository.findByPatientIdAndStatus(patientId, MedicalReportStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // ==========================================
    // Patient Self-Service: My Reports
    // ==========================================
    @Transactional(readOnly = true)
    public List<MedicalReportResponseDTO> getMyReports(Long callerUserId) {
        Long callerPatientId = resolveCallerPatientId();

        return medicalReportRepository.findByPatientIdAndStatus(callerPatientId, MedicalReportStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // ==========================================
    // Download Medical Report
    // ==========================================
    @Transactional(readOnly = true)
    public MedicalReportDownloadDTO getMedicalReportForDownload(
            Long patientId,
            Long reportId,
            Long callerUserId,
            String callerRole) {

        MedicalReport report = medicalReportRepository.findByIdAndStatus(reportId, MedicalReportStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Medical report not found with id: " + reportId));

        if (!report.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("Medical report not found with id: " + reportId);
        }

        enforcePatientAccess(patientId, callerRole);

        byte[] fileData;
        try {
            Map<?, ?> downloadResult = cloudinary.api().resource(
                    report.getStorageKey(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "type", "authenticated"
                    )
            );

            String secureUrl = (String) downloadResult.get("secure_url");

            try (InputStream inputStream = new URI(secureUrl).toURL().openStream()) {
                fileData = inputStream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve medical report from Cloudinary: {}", report.getStorageKey(), e);
            throw new IllegalStateException("Failed to retrieve medical report from Cloudinary", e);
        }

        return new MedicalReportDownloadDTO(
                report.getFileName(),
                report.getContentType(),
                fileData
        );
    }

    // ==========================================
    // Delete Medical Report (ADMIN, STAFF only)
    // ==========================================
    @Transactional
    public void deleteMedicalReport(
            Long patientId,
            Long reportId,
            Long callerUserId,
            String callerRole) {

        if (!"ADMIN".equalsIgnoreCase(callerRole) && !"STAFF".equalsIgnoreCase(callerRole)) {
            throw new AccessDeniedException("Only ADMIN and STAFF can delete medical reports");
        }

        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical report not found with id: " + reportId));

        if (!report.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("Medical report not found with id: " + reportId);
        }

        if (report.getStatus() == MedicalReportStatus.DELETED) {
            throw new ResourceNotFoundException("Medical report not found with id: " + reportId);
        }

        // Step 1: Mark deletion as PENDING in a separate new transaction
        medicalReportDeletionService.updateStatus(reportId, MedicalReportStatus.PENDING_DELETION);

        // Step 2: Attempt Cloudinary deletion
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    report.getStorageKey(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "type", "authenticated"
                    )
            );

            String deletionResult = (String) result.get("result");

            if (!"ok".equals(deletionResult) && !"not found".equals(deletionResult)) {
                throw new IllegalStateException("Cloudinary could not delete the medical report: " + deletionResult);
            }
        } catch (Exception e) {
            log.warn("Cloudinary deletion failed for report id {}. Status remains PENDING_DELETION for retry: {}",
                    reportId, e.getMessage());
            throw new IllegalStateException("Medical report deletion is pending and can be retried", e);
        }

        // Step 3: Mark deletion as DELETED
        medicalReportDeletionService.updateStatus(reportId, MedicalReportStatus.DELETED);
    }

    // ==========================================
    // Mapping Helper
    // ==========================================
    private MedicalReportResponseDTO mapToResponseDTO(MedicalReport report) {
        return new MedicalReportResponseDTO(
                report.getId(),
                report.getPatientId(),
                report.getMedicalTestId(),
                report.getFileName(),
                report.getContentType(),
                report.getFileSizeBytes(),
                report.getUploadedAt(),
                report.getUploadedByUserId()
        );
    }
}
