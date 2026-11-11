package com.cardio.clinical.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cardio.clinical.dto.DoctorResponseDTO;
import com.cardio.clinical.dto.MedicalRecordRequestDTO;
import com.cardio.clinical.dto.MedicalRecordResponseDTO;
import com.cardio.clinical.dto.MedicalRecordUpdateRequestDTO;
import com.cardio.clinical.dto.PatientProfileResponseDTO;
import com.cardio.clinical.exception.ResourceNotFoundException;
import com.cardio.clinical.model.MedicalRecord;
import com.cardio.clinical.repository.MedicalRecordRepository;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final RestClient patientServiceRestClient;
    private final RestClient doctorServiceRestClient;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            RestClient patientServiceRestClient,
            RestClient doctorServiceRestClient) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientServiceRestClient = patientServiceRestClient;
        this.doctorServiceRestClient = doctorServiceRestClient;
    }

    private MedicalRecordResponseDTO toResponseDTO(MedicalRecord record) {
        return new MedicalRecordResponseDTO(
                record.getId(),
                record.getPatientId(),
                record.getDoctorId(),
                record.getDiagnosis(),
                record.getNotes(),
                record.getRecordedAt(),
                record.getUpdatedAt());
    }

    public void validatePatientExists(Long patientId) {
        try {
            patientServiceRestClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient not found with id: " + patientId);
            }
            throw ex;
        }
    }

    public Long resolveCallerPatientId() {
        try {
            PatientProfileResponseDTO profile = patientServiceRestClient.get()
                    .uri("/api/patients/me/profile")
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);

            if (profile == null || profile.id() == null) {
                throw new ResourceNotFoundException("Patient profile not found");
            }
            return profile.id();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient profile not found. Please create your profile first.");
            }
            throw ex;
        }
    }

    public Long resolveCallerDoctorId() {
        try {
            DoctorResponseDTO doctor = doctorServiceRestClient.get()
                    .uri("/api/doctors/me/profile")
                    .retrieve()
                    .body(DoctorResponseDTO.class);

            return doctor != null ? doctor.id() : null;
        } catch (RestClientResponseException ex) {
            return null;
        }
    }

    public MedicalRecordResponseDTO createMedicalRecord(
            Long patientId,
            MedicalRecordRequestDTO request,
            String role) {

        validatePatientExists(patientId);

        Long effectiveDoctorId = request.getDoctorId();
        if (effectiveDoctorId == null && "DOCTOR".equals(role)) {
            effectiveDoctorId = resolveCallerDoctorId();
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDoctorId(effectiveDoctorId);
        record.setDiagnosis(request.getDiagnosis());
        record.setNotes(request.getNotes());

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return toResponseDTO(savedRecord);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getRecordsByPatientId(
            Long patientId,
            String role) {

        if ("PATIENT".equals(role)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!callerPatientId.equals(patientId)) {
                throw new AccessDeniedException("You can only access your own medical records");
            }
        } else {
            validatePatientExists(patientId);
        }

        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getMyRecords() {
        Long callerPatientId = resolveCallerPatientId();
        return medicalRecordRepository.findByPatientId(callerPatientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO getRecordById(Long id, String role) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        if ("PATIENT".equals(role)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!record.getPatientId().equals(callerPatientId)) {
                throw new AccessDeniedException("You can only access your own medical records");
            }
        }

        return toResponseDTO(record);
    }

    public MedicalRecordResponseDTO updateMedicalRecord(Long id, MedicalRecordUpdateRequestDTO request) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        record.setDiagnosis(request.getDiagnosis());
        record.setNotes(request.getNotes());

        MedicalRecord updatedRecord = medicalRecordRepository.save(record);
        return toResponseDTO(updatedRecord);
    }

    public void deleteMedicalRecord(Long id) {
        if (!medicalRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medical record not found with id: " + id);
        }
        medicalRecordRepository.deleteById(id);
    }
}

