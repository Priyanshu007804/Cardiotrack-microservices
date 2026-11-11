package com.cardio.doctor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cardio.doctor.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByRegistrationNumber(String registrationNumber);

    boolean existsByUserId(Long userId);

    boolean existsByRegistrationNumber(String registrationNumber);
}