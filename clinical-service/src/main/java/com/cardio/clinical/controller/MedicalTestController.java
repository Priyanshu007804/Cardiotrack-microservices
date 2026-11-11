package com.cardio.clinical.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import com.cardio.clinical.dto.MedicalTestRequestDTO;
import com.cardio.clinical.dto.MedicalTestResponseDTO;
import com.cardio.clinical.dto.MedicalTestUpdateRequestDTO;
import com.cardio.clinical.service.MedicalTestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medical-tests")
public class MedicalTestController {

    private final MedicalTestService medicalTestService;

    public MedicalTestController(MedicalTestService medicalTestService) {
        this.medicalTestService = medicalTestService;
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
    public MedicalTestResponseDTO createMedicalTest(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalTestRequestDTO request,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalTestService.createMedicalTest(patientId, request, role);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public List<MedicalTestResponseDTO> getTestsByPatientId(
            @PathVariable Long patientId,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalTestService.getTestsByPatientId(patientId, role);
    }

    @GetMapping("/my-tests")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalTestResponseDTO> getMyTests() {
        return medicalTestService.getMyTests();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR', 'PATIENT')")
    public MedicalTestResponseDTO getTestById(
            @PathVariable Long id,
            Authentication authentication) {

        String role = getRole(authentication);
        return medicalTestService.getTestById(id, role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public MedicalTestResponseDTO updateMedicalTest(
            @PathVariable Long id,
            @Valid @RequestBody MedicalTestUpdateRequestDTO request) {

        return medicalTestService.updateMedicalTest(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalTest(@PathVariable Long id) {
        medicalTestService.deleteMedicalTest(id);
    }
}

