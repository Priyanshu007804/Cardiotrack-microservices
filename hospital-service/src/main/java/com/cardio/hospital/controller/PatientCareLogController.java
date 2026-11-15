package com.cardio.hospital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cardio.hospital.dto.CareLogRequestDTO;
import com.cardio.hospital.dto.CareLogResponseDTO;
import com.cardio.hospital.service.PatientCareLogService;

import jakarta.validation.Valid;

@RestController
public class PatientCareLogController {

    private final PatientCareLogService careLogService;

    public PatientCareLogController(PatientCareLogService careLogService) {
        this.careLogService = careLogService;
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
    // Create Care Log Entry
    // (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @PostMapping("/api/patients/{patientId}/care-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public CareLogResponseDTO createCareLog(
            @PathVariable Long patientId,
            @Valid @RequestBody CareLogRequestDTO requestDTO,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        return careLogService.createCareLog(patientId, requestDTO, callerUserId, callerRole);
    }

    // ==========================================
    // Get Care Logs by Patient ID
    // (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/patients/{patientId}/care-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public List<CareLogResponseDTO> getCareLogsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        String callerRole = extractRole(authentication);
        return careLogService.getCareLogsByPatient(patientId, date, callerRole);
    }

    // ==========================================
    // Get Care Logs by Hospital Stay ID
    // (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/hospital-stays/{stayId}/care-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public List<CareLogResponseDTO> getCareLogsByStay(
            @PathVariable Long stayId,
            Authentication authentication) {

        String callerRole = extractRole(authentication);
        return careLogService.getCareLogsByHospitalStay(stayId, callerRole);
    }

    // ==========================================
    // Delete Care Log Entry
    // (ADMIN, STAFF)
    // ==========================================
    @DeleteMapping("/api/hospital-care-logs/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCareLog(@PathVariable Long logId) {
        careLogService.deleteCareLog(logId);
    }
}
