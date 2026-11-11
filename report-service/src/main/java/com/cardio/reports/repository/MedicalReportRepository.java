package com.cardio.reports.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardio.reports.model.MedicalReport;
import com.cardio.reports.model.MedicalReportStatus;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    List<MedicalReport> findByPatientId(Long patientId);

    List<MedicalReport> findByPatientIdAndStatus(Long patientId, MedicalReportStatus status);

    Optional<MedicalReport> findByIdAndStatus(Long id, MedicalReportStatus status);

    List<MedicalReport> findByStatus(MedicalReportStatus status);

    List<MedicalReport> findByMedicalTestId(Long medicalTestId);
}

