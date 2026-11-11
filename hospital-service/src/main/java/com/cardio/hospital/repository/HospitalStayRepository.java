package com.cardio.hospital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardio.hospital.model.HospitalStay;
import com.cardio.hospital.model.HospitalStayStatus;

@Repository
public interface HospitalStayRepository extends JpaRepository<HospitalStay, Long> {

    List<HospitalStay> findByPatientId(Long patientId);

    Optional<HospitalStay> findByPatientIdAndStatus(Long patientId, HospitalStayStatus status);

    boolean existsByPatientIdAndStatus(Long patientId, HospitalStayStatus status);
}

