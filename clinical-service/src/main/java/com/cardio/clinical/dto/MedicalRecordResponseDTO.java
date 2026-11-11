package com.cardio.clinical.dto;

import java.time.LocalDateTime;

public record MedicalRecordResponseDTO(
        Long id,
        Long patientId,
        Long doctorId,
        String diagnosis,
        String notes,
        LocalDateTime recordedAt,
        LocalDateTime updatedAt) {
}

