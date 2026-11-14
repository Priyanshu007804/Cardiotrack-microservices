package com.cardio.patient.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardio.patient.dto.PatientRequestDTO;
import com.cardio.patient.dto.PatientResponseDTO;
import com.cardio.patient.dto.PatientUpdateRequestDTO;
import com.cardio.patient.event.PatientRegisteredEvent;
import com.cardio.patient.exception.ResourceNotFoundException;
import com.cardio.patient.model.Patient;
import com.cardio.patient.repository.PatientRepository;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PatientService(
            PatientRepository patientRepository,
            ApplicationEventPublisher eventPublisher) {
        this.patientRepository = patientRepository;
        this.eventPublisher = eventPublisher;
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
        return createPatient(request, null);
    }

    public PatientResponseDTO createPatient(PatientRequestDTO request, Long userId) {

        Patient patient = new Patient();

        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        if (userId != null) {
            patient.setUserId(userId);
        }

        Patient savedPatient = patientRepository.save(patient);

        // Publish event for transaction-committed Kafka processing
        PatientRegisteredEvent event = PatientRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PATIENT_REGISTERED")
                .patientId(savedPatient.getId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishEvent(event);

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

        // Publish event for transaction-committed Kafka processing
        PatientRegisteredEvent event = PatientRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PATIENT_REGISTERED")
                .patientId(savedPatient.getId())
                .timestamp(Instant.now())
                .build();
        eventPublisher.publishEvent(event);

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