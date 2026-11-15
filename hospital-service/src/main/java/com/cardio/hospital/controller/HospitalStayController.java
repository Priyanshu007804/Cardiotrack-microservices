package com.cardio.hospital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cardio.hospital.dto.HospitalStayRequestDTO;
import com.cardio.hospital.dto.HospitalStayResponseDTO;
import com.cardio.hospital.service.HospitalStayService;

import jakarta.validation.Valid;

@RestController
public class HospitalStayController {

    private final HospitalStayService hospitalStayService;

    public HospitalStayController(HospitalStayService hospitalStayService) {
        this.hospitalStayService = hospitalStayService;
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
    // Admit Patient (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @PostMapping("/api/patients/{patientId}/hospital-stays")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalStayResponseDTO admitPatient(
            @PathVariable Long patientId,
            @Valid @RequestBody HospitalStayRequestDTO requestDTO,
            Authentication authentication) {

        String callerRole = extractRole(authentication);
        return hospitalStayService.admitPatient(patientId, requestDTO, callerRole);
    }

    // ==========================================
    // Get All Stays across all patients (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @GetMapping("/api/hospital-stays")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public List<HospitalStayResponseDTO> getAllStays(Authentication authentication) {
        String callerRole = extractRole(authentication);
        return hospitalStayService.getAllStays(callerRole);
    }

    // ==========================================
    // Get Stays by Patient ID (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/patients/{patientId}/hospital-stays")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public List<HospitalStayResponseDTO> getStaysByPatientId(
            @PathVariable Long patientId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        return hospitalStayService.getStaysByPatientId(patientId, callerUserId, callerRole);
    }

    // ==========================================
    // Patient Self-Service: My Stays (PATIENT)
    // ==========================================
    @GetMapping("/api/hospital-stays/my-stays")
    @PreAuthorize("hasRole('PATIENT')")
    public List<HospitalStayResponseDTO> getMyStays(Authentication authentication) {
        Long callerUserId = extractUserId(authentication);
        return hospitalStayService.getMyStays(callerUserId);
    }

    // ==========================================
    // Get Single Stay by ID (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/hospital-stays/{stayId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public HospitalStayResponseDTO getStayById(
            @PathVariable Long stayId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        return hospitalStayService.getStayById(stayId, callerUserId, callerRole);
    }

    // ==========================================
    // Get Current Active Stay (ADMIN, STAFF, DOCTOR, PATIENT)
    // ==========================================
    @GetMapping("/api/patients/{patientId}/hospital-stays/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public HospitalStayResponseDTO getActiveStay(
            @PathVariable Long patientId,
            Authentication authentication) {

        Long callerUserId = extractUserId(authentication);
        String callerRole = extractRole(authentication);

        return hospitalStayService.getActiveStay(patientId, callerUserId, callerRole);
    }

    // ==========================================
    // Discharge Patient (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @PutMapping("/api/patients/{patientId}/hospital-stays/{stayId}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public HospitalStayResponseDTO dischargePatient(
            @PathVariable Long patientId,
            @PathVariable Long stayId,
            Authentication authentication) {

        String callerRole = extractRole(authentication);
        return hospitalStayService.dischargePatient(patientId, stayId, callerRole);
    }
}

