package com.cardio.clinical.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import com.cardio.clinical.dto.MedicalRecordRequestDTO;
import com.cardio.clinical.dto.MedicalRecordResponseDTO;
import com.cardio.clinical.dto.MedicalRecordUpdateRequestDTO;
import com.cardio.clinical.service.MedicalRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("")
                .replace("ROLE_", "");
    }

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponseDTO createMedicalRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordRequestDTO request,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalRecordService.createMedicalRecord(patientId, request, role);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public List<MedicalRecordResponseDTO> getRecordsByPatientId(
            @PathVariable Long patientId,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalRecordService.getRecordsByPatientId(patientId, role);
    }

    @GetMapping("/my-records")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalRecordResponseDTO> getMyRecords() {
        return medicalRecordService.getMyRecords();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public MedicalRecordResponseDTO getRecordById(
            @PathVariable Long id,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalRecordService.getRecordById(id, role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public MedicalRecordResponseDTO updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateRequestDTO request) {

        return medicalRecordService.updateMedicalRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
    }
}

