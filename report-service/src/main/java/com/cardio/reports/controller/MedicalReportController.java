package com.cardio.reports.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cardio.reports.dto.MedicalReportDownloadDTO;
import com.cardio.reports.dto.MedicalReportResponseDTO;
import com.cardio.reports.service.MedicalReportService;

@RestController
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("")
                .replace("ROLE_", "");
    }

    // ==========================================
    // Upload Medical Report (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @PostMapping(value = "/api/patients/{patientId}/medical-reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<MedicalReportResponseDTO> uploadMedicalReport(
            @PathVariable Long patientId,
            @RequestParam(value = "medicalTestId", required = false) Long medicalTestId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        MedicalReportResponseDTO response = medicalReportService.uploadMedicalReport(
                patientId, medicalTestId, file, callerUserId, callerRole);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==========================================
    // Get Reports by Patient ID (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/patients/{patientId}/medical-reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<MedicalReportResponseDTO>> getReportsByPatientId(
            @PathVariable Long patientId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        List<MedicalReportResponseDTO> reports = medicalReportService.getReportsByPatientId(
                patientId, callerUserId, callerRole);

        return ResponseEntity.ok(reports);
    }

    // ==========================================
    // Patient Self-Service: My Reports (PATIENT)
    // ==========================================
    @GetMapping("/api/medical-reports/my-reports")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalReportResponseDTO>> getMyReports(
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);

        List<MedicalReportResponseDTO> reports = medicalReportService.getMyReports(callerUserId);

        return ResponseEntity.ok(reports);
    }

    // ==========================================
    // Download Report (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/patients/{patientId}/medical-reports/{reportId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ByteArrayResource> downloadMedicalReport(
            @PathVariable Long patientId,
            @PathVariable Long reportId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        MedicalReportDownloadDTO report = medicalReportService.getMedicalReportForDownload(
                patientId, reportId, callerUserId, callerRole);

        ByteArrayResource resource = new ByteArrayResource(report.getFileData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.getContentType()))
                .header("Content-Disposition", "attachment; filename=\"" + report.getFileName() + "\"")
                .contentLength(report.getFileData().length)
                .body(resource);
    }

    // ==========================================
    // Delete Report (ADMIN, STAFF only)
    // ==========================================
    @DeleteMapping("/api/patients/{patientId}/medical-reports/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalReport(
            @PathVariable Long patientId,
            @PathVariable Long reportId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        medicalReportService.deleteMedicalReport(patientId, reportId, callerUserId, callerRole);
    }
}

