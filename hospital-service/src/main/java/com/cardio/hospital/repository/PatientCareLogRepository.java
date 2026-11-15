package com.cardio.hospital.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardio.hospital.model.PatientCareLog;

@Repository
public interface PatientCareLogRepository extends JpaRepository<PatientCareLog, Long> {

    List<PatientCareLog> findByPatientIdOrderByLogDateDescLogTimeDesc(Long patientId);

    List<PatientCareLog> findByPatientIdAndLogDateOrderByLogTimeDesc(Long patientId, LocalDate logDate);

    List<PatientCareLog> findByHospitalStayIdOrderByLogDateDescLogTimeDesc(Long hospitalStayId);
}
