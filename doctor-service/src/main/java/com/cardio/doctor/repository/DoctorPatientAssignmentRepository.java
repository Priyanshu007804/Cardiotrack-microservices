package com.cardio.doctor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cardio.doctor.model.DoctorPatientAssignment;

public interface DoctorPatientAssignmentRepository
                extends JpaRepository<DoctorPatientAssignment, Long> {

        boolean existsByDoctorIdAndPatientIdAndActiveTrue(
                        Long doctorId, Long patientId);

        Optional<DoctorPatientAssignment> findByDoctorIdAndPatientId(
                        Long doctorId, Long patientId);

        List<DoctorPatientAssignment> findByDoctorIdAndActiveTrue(
                        Long doctorId);

        List<DoctorPatientAssignment> findByPatientIdAndActiveTrue(
                        Long patientId);

        List<DoctorPatientAssignment> findByActiveTrue();
}