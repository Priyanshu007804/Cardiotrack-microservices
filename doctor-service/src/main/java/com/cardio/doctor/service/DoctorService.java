package com.cardio.doctor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardio.doctor.dto.DoctorRequestDTO;
import com.cardio.doctor.dto.DoctorResponseDTO;
import com.cardio.doctor.dto.DoctorUpdateRequestDTO;
import com.cardio.doctor.exception.DuplicateResourceException;
import com.cardio.doctor.exception.ResourceNotFoundException;
import com.cardio.doctor.model.Doctor;
import com.cardio.doctor.model.DoctorPatientAssignment;
import com.cardio.doctor.repository.DoctorPatientAssignmentRepository;
import com.cardio.doctor.repository.DoctorRepository;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;

    public DoctorService(
            DoctorRepository doctorRepository,
            DoctorPatientAssignmentRepository assignmentRepository) {
        this.doctorRepository = doctorRepository;
        this.assignmentRepository = assignmentRepository;
    }

    private DoctorResponseDTO toResponseDTO(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getUserId(),
                doctor.getFullName(),
                doctor.getQualification(),
                doctor.getSpecialization(),
                doctor.getExperienceYears(),
                doctor.getRegistrationNumber());
    }

    public DoctorResponseDTO createDoctor(
            Long userId, DoctorRequestDTO request) {

        if (doctorRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException(
                    "Doctor profile already exists for this user");
        }

        if (doctorRepository.existsByRegistrationNumber(
                request.registrationNumber())) {
            throw new DuplicateResourceException(
                    "Registration number already exists");
        }

        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setFullName(request.fullName());
        doctor.setQualification(request.qualification());
        doctor.setSpecialization(request.specialization());
        doctor.setExperienceYears(request.experienceYears());
        doctor.setRegistrationNumber(request.registrationNumber());

        Doctor savedDoctor = doctorRepository.save(doctor);

        return toResponseDTO(savedDoctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + id));

        return toResponseDTO(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByUserId(Long userId) {

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor profile not found for user: " + userId));

        return toResponseDTO(doctor);
    }

    public DoctorResponseDTO updateDoctor(
            Long id, DoctorUpdateRequestDTO request) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + id));

        doctor.setFullName(request.fullName());
        doctor.setQualification(request.qualification());
        doctor.setSpecialization(request.specialization());
        doctor.setExperienceYears(request.experienceYears());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return toResponseDTO(updatedDoctor);
    }

    public void deleteDoctor(Long id) {

        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Doctor not found with id: " + id);
        }

        // Deactivate active assignments before deletion to prevent orphaned records and preserve history
        List<DoctorPatientAssignment> activeAssignments =
                assignmentRepository.findByDoctorIdAndActiveTrue(id);
        for (DoctorPatientAssignment assignment : activeAssignments) {
            assignment.setActive(false);
        }
        assignmentRepository.saveAll(activeAssignments);

        doctorRepository.deleteById(id);
    }
}
