package com.cardio.clinical.dto;

import java.time.LocalDateTime;

public record MedicalTestResponseDTO(
        Long id,
        Long patientId,
        Long doctorId,
        Long hospitalStayId,
        String testName,
        String testType,
        LocalDateTime testDate,
        String results,
        String interpretation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

