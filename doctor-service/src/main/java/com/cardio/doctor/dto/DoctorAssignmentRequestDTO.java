package com.cardio.doctor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DoctorAssignmentRequestDTO(

        @NotNull(message = "Patient ID is required") @Positive(message = "Patient ID must be positive") Long patientId

) {
}