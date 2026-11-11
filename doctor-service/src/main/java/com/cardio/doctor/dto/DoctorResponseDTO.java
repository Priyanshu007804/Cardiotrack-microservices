package com.cardio.doctor.dto;

public record DoctorResponseDTO(
                Long id,
                Long userId,
                String fullName,
                String qualification,
                String specialization,
                Integer experienceYears,
                String registrationNumber) {
}