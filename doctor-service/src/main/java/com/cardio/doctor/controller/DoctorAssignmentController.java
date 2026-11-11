package com.cardio.doctor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cardio.doctor.dto.DoctorAssignmentResponseDTO;
import com.cardio.doctor.service.DoctorAssignmentService;

@RestController
@RequestMapping("/api/doctor-assignments")
public class DoctorAssignmentController {

    private final DoctorAssignmentService assignmentService;

    public DoctorAssignmentController(
            DoctorAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorAssignmentResponseDTO assignPatient(
            @RequestParam Long doctorId,
            @RequestParam Long patientId) {

        return assignmentService.assignPatient(doctorId, patientId);
    }

    @GetMapping("/my-doctors")
    @PreAuthorize("hasRole('PATIENT')")
    public List<DoctorAssignmentResponseDTO> getMyAssignedDoctors() {
        return assignmentService.getMyAssignedDoctors();
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public List<DoctorAssignmentResponseDTO> getPatientsByDoctor(
            @PathVariable Long doctorId) {

        return assignmentService.getPatientsByDoctor(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public List<DoctorAssignmentResponseDTO> getDoctorsByPatient(
            @PathVariable Long patientId) {

        return assignmentService.getDoctorsByPatient(patientId);
    }

    @DeleteMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateAssignment(
            @PathVariable Long doctorId,
            @PathVariable Long patientId) {

        assignmentService.deactivateAssignment(doctorId, patientId);
    }
}
