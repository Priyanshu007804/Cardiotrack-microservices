package com.cardio.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cardio.hospital.dto.CareLogRequestDTO;
import com.cardio.hospital.dto.CareLogResponseDTO;
import com.cardio.hospital.dto.PatientProfileResponseDTO;
import com.cardio.hospital.exception.ResourceNotFoundException;
import com.cardio.hospital.model.CareLogCategory;
import com.cardio.hospital.model.PatientCareLog;
import com.cardio.hospital.repository.PatientCareLogRepository;

@Service
public class PatientCareLogService {

    private static final Logger log = LoggerFactory.getLogger(PatientCareLogService.class);

    private final PatientCareLogRepository careLogRepository;
    private final RestClient patientServiceRestClient;

    public PatientCareLogService(
            PatientCareLogRepository careLogRepository,
            RestClient patientServiceRestClient) {
        this.careLogRepository = careLogRepository;
        this.patientServiceRestClient = patientServiceRestClient;
    }

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
            log.warn("Patient validation warning: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Patient service connectivity check skipped: {}", ex.getMessage());
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
            try {
                Long callerPatientId = resolveCallerPatientId();
                if (!callerPatientId.equals(targetPatientId)) {
                    throw new AccessDeniedException("You can only access care logs for your own profile");
                }
            } catch (ResourceNotFoundException ex) {
                // Allow fallback if profile lookup fails
                log.warn("Patient ownership check warning: {}", ex.getMessage());
            }
        }
    }

    // ==========================================
    // Create Care Log
    // ==========================================
    @Transactional
    public CareLogResponseDTO createCareLog(
            Long patientId,
            CareLogRequestDTO requestDTO,
            Long callerUserId,
            String callerRole) {

        validatePatientExists(patientId);

        LocalDate date = requestDTO.getLogDate() != null ? requestDTO.getLogDate() : LocalDate.now();
        LocalTime time = requestDTO.getLogTime() != null ? requestDTO.getLogTime() : LocalTime.now();
        CareLogCategory category = requestDTO.getCategory() != null ? requestDTO.getCategory() : CareLogCategory.GENERAL;
        String status = (requestDTO.getStatus() != null && !requestDTO.getStatus().isBlank())
                ? requestDTO.getStatus()
                : "COMPLETED";

        String recordedBy = requestDTO.getRecordedBy();
        if (recordedBy == null || recordedBy.isBlank()) {
            recordedBy = ("DOCTOR".equalsIgnoreCase(callerRole)) ? "Attending Clinician" : "Hospital Care Team";
        }

        PatientCareLog logEntry = new PatientCareLog(
                patientId,
                requestDTO.getHospitalStayId(),
                date,
                time,
                category,
                requestDTO.getTitle(),
                requestDTO.getDescription(),
                recordedBy,
                callerRole,
                status
        );

        PatientCareLog saved = careLogRepository.save(logEntry);
        log.info("Care log created [id={}, patientId={}, category='{}', title='{}', by='{}']",
                saved.getId(), patientId, category, saved.getTitle(), recordedBy);

        return CareLogResponseDTO.fromEntity(saved);
    }

    // ==========================================
    // Get Care Logs by Patient
    // ==========================================
    @Transactional(readOnly = true)
    public List<CareLogResponseDTO> getCareLogsByPatient(
            Long patientId,
            LocalDate dateFilter,
            String callerRole) {

        enforcePatientAccess(patientId, callerRole);

        List<PatientCareLog> logs;
        if (dateFilter != null) {
            logs = careLogRepository.findByPatientIdAndLogDateOrderByLogTimeDesc(patientId, dateFilter);
        } else {
            logs = careLogRepository.findByPatientIdOrderByLogDateDescLogTimeDesc(patientId);
        }

        return logs.stream()
                .map(CareLogResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==========================================
    // Get Care Logs by Hospital Stay
    // ==========================================
    @Transactional(readOnly = true)
    public List<CareLogResponseDTO> getCareLogsByHospitalStay(
            Long hospitalStayId,
            String callerRole) {

        List<PatientCareLog> logs = careLogRepository.findByHospitalStayIdOrderByLogDateDescLogTimeDesc(hospitalStayId);

        return logs.stream()
                .map(CareLogResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==========================================
    // Delete Care Log
    // ==========================================
    @Transactional
    public void deleteCareLog(Long logId) {
        PatientCareLog logEntry = careLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Care log not found with id: " + logId));

        careLogRepository.delete(logEntry);
        log.info("Care log deleted with id: {}", logId);
    }
}
