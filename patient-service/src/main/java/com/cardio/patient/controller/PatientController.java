package com.cardio.patient.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cardio.patient.dto.PatientRequestDTO;
import com.cardio.patient.dto.PatientResponseDTO;
import com.cardio.patient.dto.PatientUpdateRequestDTO;
import com.cardio.patient.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // Create patient (ADMIN, STAFF, DOCTOR)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponseDTO createPatient(
            @Valid @RequestBody PatientRequestDTO request) {

        return patientService.createPatient(request);
    }

    // Get all patients
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public List<PatientResponseDTO> getAllPatients() {

        return patientService.getAllPatients();
    }

    // Get patient by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public PatientResponseDTO getPatientById(
            @PathVariable Long id) {

        return patientService.getPatientById(id);
    }

    // Delete patient (ADMIN only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable Long id) {

        patientService.deletePatient(id);
    }

    // Update patient (ADMIN, STAFF, DOCTOR)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public PatientResponseDTO updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateRequestDTO request) {

        return patientService.updatePatient(id, request);
    }

    // Create authenticated patient's own profile
    @PostMapping("/me/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponseDTO createMyProfile(
            @Valid @RequestBody PatientRequestDTO request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return patientService.createMyPatientProfile(userId, request);
    }

    // Get authenticated patient's own profile
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponseDTO getMyProfile(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return patientService.getMyPatientProfile(userId);
    }

    // Update authenticated patient's own profile
    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponseDTO updateMyProfile(
            @Valid @RequestBody PatientUpdateRequestDTO request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return patientService.updateMyPatientProfile(userId, request);
    }
}