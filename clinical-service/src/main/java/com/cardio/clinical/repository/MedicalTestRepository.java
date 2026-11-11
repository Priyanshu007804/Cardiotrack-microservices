package com.cardio.clinical.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardio.clinical.model.MedicalTest;

@Repository
public interface MedicalTestRepository extends JpaRepository<MedicalTest, Long> {

    List<MedicalTest> findByPatientId(Long patientId);

    List<MedicalTest> findByDoctorId(Long doctorId);

    List<MedicalTest> findByHospitalStayId(Long hospitalStayId);
}

