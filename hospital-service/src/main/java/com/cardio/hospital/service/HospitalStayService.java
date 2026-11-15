package com.cardio.hospital.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.cardio.hospital.dto.HospitalStayRequestDTO;
import com.cardio.hospital.dto.HospitalStayResponseDTO;
import com.cardio.hospital.dto.PatientProfileResponseDTO;
import com.cardio.hospital.exception.ResourceNotFoundException;
import com.cardio.hospital.model.HospitalStay;
import com.cardio.hospital.model.HospitalStayStatus;
import com.cardio.hospital.repository.HospitalStayRepository;

@Service
public class HospitalStayService {

    private final HospitalStayRepository hospitalStayRepository;
    private final RestClient patientServiceRestClient;

    public HospitalStayService(
            HospitalStayRepository hospitalStayRepository,
            RestClient patientServiceRestClient) {
        this.hospitalStayRepository = hospitalStayRepository;
        this.patientServiceRestClient = patientServiceRestClient;
    }

    // ==========================================
    // Downstream Patient Service Helpers
    // ==========================================
    private void validatePatientExists(Long patientId) {
        try {
            patientServiceRestClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient not found with id: " + patientId);
            }
            throw ex;
        }
    }

    private Long resolveCallerPatientId() {
        try {
            PatientProfileResponseDTO profile = patientServiceRestClient.get()
                    .uri("/api/patients/me/profile")
                    .retrieve()
                    .body(PatientProfileResponseDTO.class);

            if (profile == null || profile.getId() == null) {
                throw new ResourceNotFoundException("Patient profile not found for authenticated user");
            }

            return profile.getId();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Patient profile not found for authenticated user");
            }
            throw ex;
        }
    }

    private void enforcePatientAccess(Long targetPatientId, String callerRole) {
        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            Long callerPatientId = resolveCallerPatientId();
            if (!callerPatientId.equals(targetPatientId)) {
                throw new AccessDeniedException("You can only access your own hospital stays");
            }
        }
    }

    // ==========================================
    // Admit Patient (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @Transactional
    public HospitalStayResponseDTO admitPatient(
            Long patientId,
            HospitalStayRequestDTO requestDTO,
            String callerRole) {

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            throw new AccessDeniedException("Patients are not authorized to perform admissions");
        }

        // 1. Verify patient exists in Patient Service
        validatePatientExists(patientId);

        // 2. Guard against multiple concurrent active admissions
        boolean alreadyAdmitted = hospitalStayRepository.existsByPatientIdAndStatus(
                patientId, HospitalStayStatus.ADMITTED);
        if (alreadyAdmitted) {
            throw new IllegalStateException("Patient is already admitted. Please discharge the active stay before admitting again.");
        }

        // 3. Create hospital stay record
        HospitalStay hospitalStay = new HospitalStay();
        hospitalStay.setPatientId(patientId);
        hospitalStay.setAdmissionReason(requestDTO.getAdmissionReason());
        hospitalStay.setNotes(requestDTO.getNotes());
        hospitalStay.setAdmissionDate(LocalDateTime.now());
        hospitalStay.setDischargeDate(null);
        hospitalStay.setStatus(HospitalStayStatus.ADMITTED);

        HospitalStay savedStay = hospitalStayRepository.save(hospitalStay);

        return toResponseDTO(savedStay);
    }

    // ==========================================
    // Get All Stays across all patients (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @Transactional(readOnly = true)
    public List<HospitalStayResponseDTO> getAllStays(String callerRole) {
        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            throw new AccessDeniedException("Patients are not authorized to view all hospital stays");
        }

        return hospitalStayRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==========================================
    // Get Stays by Patient ID
    // ==========================================
    @Transactional(readOnly = true)
    public List<HospitalStayResponseDTO> getStaysByPatientId(
            Long patientId,
            Long callerUserId,
            String callerRole) {

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            enforcePatientAccess(patientId, callerRole);
        } else {
            validatePatientExists(patientId);
        }

        return hospitalStayRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==========================================
    // Patient Self-Service: My Stays
    // ==========================================
    @Transactional(readOnly = true)
    public List<HospitalStayResponseDTO> getMyStays(Long callerUserId) {
        Long callerPatientId = resolveCallerPatientId();

        return hospitalStayRepository.findByPatientId(callerPatientId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==========================================
    // Get Single Stay by ID
    // ==========================================
    @Transactional(readOnly = true)
    public HospitalStayResponseDTO getStayById(
            Long stayId,
            Long callerUserId,
            String callerRole) {

        HospitalStay stay = hospitalStayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital stay not found with id: " + stayId));

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            enforcePatientAccess(stay.getPatientId(), callerRole);
        }

        return toResponseDTO(stay);
    }

    // ==========================================
    // Get Current Active Stay for Patient
    // ==========================================
    @Transactional(readOnly = true)
    public HospitalStayResponseDTO getActiveStay(
            Long patientId,
            Long callerUserId,
            String callerRole) {

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            enforcePatientAccess(patientId, callerRole);
        } else {
            validatePatientExists(patientId);
        }

        HospitalStay activeStay = hospitalStayRepository.findByPatientIdAndStatus(
                patientId, HospitalStayStatus.ADMITTED)
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found for patient with id: " + patientId));

        return toResponseDTO(activeStay);
    }

    // ==========================================
    // Discharge Patient (ADMIN, STAFF, DOCTOR)
    // ==========================================
    @Transactional
    public HospitalStayResponseDTO dischargePatient(
            Long patientId,
            Long stayId,
            String callerRole) {

        if ("PATIENT".equalsIgnoreCase(callerRole)) {
            throw new AccessDeniedException("Patients are not authorized to perform discharges");
        }

        HospitalStay stay = hospitalStayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital stay not found with id: " + stayId));

        if (!stay.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("Hospital stay does not belong to patient with id: " + patientId);
        }

        if (stay.getStatus() == HospitalStayStatus.DISCHARGED) {
            throw new IllegalStateException("Patient has already been discharged.");
        }

        stay.setDischargeDate(LocalDateTime.now());
        stay.setStatus(HospitalStayStatus.DISCHARGED);

        HospitalStay updatedStay = hospitalStayRepository.save(stay);
        return toResponseDTO(updatedStay);
    }

    // ==========================================
    // Mapping Helper
    // ==========================================
    private HospitalStayResponseDTO toResponseDTO(HospitalStay stay) {
        return new HospitalStayResponseDTO(
                stay.getId(),
                stay.getPatientId(),
                stay.getAdmissionDate(),
                stay.getDischargeDate(),
                stay.getAdmissionReason(),
                stay.getNotes(),
                stay.getStatus()
        );
    }
}

