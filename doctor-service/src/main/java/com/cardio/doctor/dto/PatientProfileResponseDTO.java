package com.cardio.doctor.dto;

public record PatientProfileResponseDTO(
        Long id,
        String name,
        Integer age,
        String gender) {
}

