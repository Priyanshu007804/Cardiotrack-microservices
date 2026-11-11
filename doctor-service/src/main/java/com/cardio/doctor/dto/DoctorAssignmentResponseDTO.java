package com.cardio.doctor.dto;

import java.time.LocalDateTime;

public record DoctorAssignmentResponseDTO(
        Long id,
        Long doctorId,
        Long patientId,
        boolean active,
        LocalDateTime assignedAt) {
}