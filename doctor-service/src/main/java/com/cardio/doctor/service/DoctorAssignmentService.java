package com.cardio.doctor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cardio.doctor.dto.DoctorAssignmentResponseDTO;
import com.cardio.doctor.dto.PatientProfileResponseDTO;
import com.cardio.doctor.exception.ResourceNotFoundException;
import com.cardio.doctor.model.DoctorPatientAssignment;
import com.cardio.doctor.repository.DoctorPatientAssignmentRepository;
import com.cardio.doctor.repository.DoctorRepository;

@Service
@Transactional
public class DoctorAssignmentService {

    private final DoctorPatientAssignmentRepository assignmentRepository;
    private final DoctorRepository doctorRepository;
    private final RestClient patientServiceRestClient;

    public DoctorAssignmentService(
            DoctorPatientAssignmentRepository assignmentRepository,
            DoctorRepository doctorRepository,
            RestClient patientServiceRestClient) {

        this.assignmentRepository = assignmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientServiceRestClient = patientServiceRestClient;
    }

    private DoctorAssignmentResponseDTO toResponseDTO(
            DoctorPatientAssignment assignment) {

        return new DoctorAssignmentResponseDTO(
                assignment.getId(),
                assignment.getDoctorId(),
                assignment.getPatientId(),
                assignment.isActive(),
                assignment.getAssignedAt());
    }

    private void validatePatientExists(Long patientId) {

        try {
            patientServiceRestClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException exception) {

            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Patient not found with id: " + patientId);
            }

            throw exception;
        }
    }

    public DoctorAssignmentResponseDTO assignPatient(Long doctorId, Long patientId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }

        validatePatientExists(patientId);

        var existingAssignment = assignmentRepository.findByDoctorIdAndPatientId(doctorId,
                patientId);

        if (existingAssignment.isPresent()) {
            DoctorPatientAssignment assignment = existingAssignment.get();

            if (assignment.isActive()) {
                throw new IllegalStateException("Patient is already assigned to this doctor");
            }

            assignment.setActive(true);
            return toResponseDTO(assignmentRepository.save(assignment));
        }

        DoctorPatientAssignment assignment = new DoctorPatientAssignment();
        assignment.setDoctorId(doctorId);
        assignment.setPatientId(patientId);
        assignment.setActive(true);

        return toResponseDTO(assignmentRepository.save(assignment));
    }

    public List<DoctorAssignmentResponseDTO> getPatientsByDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }

        return assignmentRepository.findByDoctorIdAndActiveTrue(doctorId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<DoctorAssignmentResponseDTO> getDoctorsByPatient(Long patientId) {
        return assignmentRepository.findByPatientIdAndActiveTrue(patientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<DoctorAssignmentResponseDTO> getMyAssignedDoctors() {
        PatientProfileResponseDTO patientProfile;

        try {
            patientProfile = patientServiceRestClient.get()
                    .uri("/api/patients/me/profile")
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Patient profile not found. Please create your patient profile first.");
            }
            throw exception;
        }

        if (patientProfile == null || patientProfile.id() == null) {
            throw new ResourceNotFoundException("Patient profile not found");
        }

        return assignmentRepository.findByPatientIdAndActiveTrue(patientProfile.id())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void deactivateAssignment(
            Long doctorId, Long patientId) {

        DoctorPatientAssignment assignment = assignmentRepository
                .findByDoctorIdAndPatientId(doctorId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found"));

        if (!assignment.isActive()) {
            throw new IllegalStateException(
                    "Assignment is already inactive");
        }

        assignment.setActive(false);
        assignmentRepository.save(assignment);
    }
}
