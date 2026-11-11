package com.cardio.clinical.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cardio.clinical.dto.DoctorResponseDTO;
import com.cardio.clinical.dto.MedicalTestRequestDTO;
import com.cardio.clinical.dto.MedicalTestResponseDTO;
import com.cardio.clinical.dto.MedicalTestUpdateRequestDTO;
import com.cardio.clinical.dto.PatientProfileResponseDTO;
import com.cardio.clinical.exception.ResourceNotFoundException;
import com.cardio.clinical.model.MedicalTest;
import com.cardio.clinical.repository.MedicalTestRepository;

@Service
@Transactional
public class MedicalTestService {

    private final MedicalTestRepository medicalTestRepository;
    private final RestClient patientServiceRestClient;
    private final RestClient doctorServiceRestClient;

    public MedicalTestService(
            MedicalTestRepository medicalTestRepository,
            RestClient patientServiceRestClient,
            RestClient doctorServiceRestClient) {
        this.medicalTestRepository = medicalTestRepository;
        this.patientServiceRestClient = patientServiceRestClient;
        this.doctorServiceRestClient = doctorServiceRestClient;
    }

    private MedicalTestResponseDTO toResponseDTO(MedicalTest test) {
        return new MedicalTestResponseDTO(
                test.getId(),
                test.getPatientId(),
                test.getDoctorId(),
                test.getHospitalStayId(),
                test.getTestName(),
                test.getTestType(),
                test.getTestDate(),
                test.getResults(),
                test.getInterpretation(),
                test.getCreatedAt(),
                test.getUpdatedAt());
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

    public MedicalTestResponseDTO createMedicalTest(
            Long patientId,
            MedicalTestRequestDTO request,
            String role) {

        validatePatientExists(patientId);

        Long effectiveDoctorId = request.getDoctorId();
        if (effectiveDoctorId == null && "DOCTOR".equals(role)) {
            effectiveDoctorId = resolveCallerDoctorId();
        }

        MedicalTest test = new MedicalTest();
        test.setPatientId(patientId);
        test.setDoctorId(effectiveDoctorId);
        test.setHospitalStayId(request.getHospitalStayId());
        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setTestDate(request.getTestDate());
        test.setResults(request.getResults());
        test.setInterpretation(request.getInterpretation());

        MedicalTest savedTest = medicalTestRepository.save(test);
        return toResponseDTO(savedTest);
    }

    @Transactional(readOnly = true)
    public List<MedicalTestResponseDTO> getTestsByPatientId(
            Long patientId,
            String role) {

        if ("PATIENT".equals(role)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!callerPatientId.equals(patientId)) {
                throw new AccessDeniedException("You can only access your own medical tests");
            }
        } else {
            validatePatientExists(patientId);
        }

        return medicalTestRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalTestResponseDTO> getMyTests() {
        Long callerPatientId = resolveCallerPatientId();
        return medicalTestRepository.findByPatientId(callerPatientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicalTestResponseDTO getTestById(Long id, String role) {
        MedicalTest test = medicalTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical test not found with id: " + id));

        if ("PATIENT".equals(role)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!test.getPatientId().equals(callerPatientId)) {
                throw new AccessDeniedException("You can only access your own medical tests");
            }
        }

        return toResponseDTO(test);
    }

    public MedicalTestResponseDTO updateMedicalTest(Long id, MedicalTestUpdateRequestDTO request) {
        MedicalTest test = medicalTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical test not found with id: " + id));

        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setTestDate(request.getTestDate());
        test.setResults(request.getResults());
        test.setInterpretation(request.getInterpretation());

        MedicalTest updatedTest = medicalTestRepository.save(test);
        return toResponseDTO(updatedTest);
    }

    public void deleteMedicalTest(Long id) {
        if (!medicalTestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medical test not found with id: " + id);
        }
        medicalTestRepository.deleteById(id);
    }
}

