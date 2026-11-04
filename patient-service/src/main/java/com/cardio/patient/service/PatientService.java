package com.cardio.patient.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardio.patient.dto.PatientRequestDTO;
import com.cardio.patient.dto.PatientResponseDTO;
import com.cardio.patient.dto.PatientUpdateRequestDTO;
import com.cardio.patient.exception.ResourceNotFoundException;
import com.cardio.patient.model.Patient;
import com.cardio.patient.repository.PatientRepository;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // Entity to DTO mapping
    private PatientResponseDTO toResponseDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender()
        );
    }

    // Create patient (Admin / Staff / Doctor)
    public PatientResponseDTO createPatient(PatientRequestDTO request) {

        Patient patient = new Patient();

        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());

        Patient savedPatient = patientRepository.save(patient);

        return toResponseDTO(savedPatient);
    }

    // Get all patients
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {

        return patientRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Get patient by ID
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id: " + id
                        ));

        return toResponseDTO(patient);
    }

    // Delete patient
    public void deletePatient(Long id) {

        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Patient not found with id: " + id
            );
        }

        patientRepository.deleteById(id);
    }

    // Update patient by ID
    public PatientResponseDTO updatePatient(
            Long id,
            PatientUpdateRequestDTO request) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id: " + id
                        ));

        existingPatient.setName(request.getName());
        existingPatient.setAge(request.getAge());
        existingPatient.setGender(request.getGender());

        Patient updatedPatient = patientRepository.save(existingPatient);

        return toResponseDTO(updatedPatient);
    }

    // Create authenticated user's own patient profile
    public PatientResponseDTO createMyPatientProfile(
            Long userId,
            PatientRequestDTO request) {

        if (patientRepository.existsByUserId(userId)) {
            throw new IllegalStateException(
                    "Patient profile already exists for this user"
            );
        }

        Patient patient = new Patient();

        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setUserId(userId);

        Patient savedPatient = patientRepository.save(patient);

        return toResponseDTO(savedPatient);
    }

    // Get authenticated user's own patient profile
    @Transactional(readOnly = true)
    public PatientResponseDTO getMyPatientProfile(Long userId) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient profile not found"
                        ));

        return toResponseDTO(patient);
    }

    // Update authenticated user's own patient profile
    public PatientResponseDTO updateMyPatientProfile(
            Long userId,
            PatientUpdateRequestDTO request) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient profile not found"
                        ));

        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());

        Patient updatedPatient = patientRepository.save(patient);

        return toResponseDTO(updatedPatient);
    }
}