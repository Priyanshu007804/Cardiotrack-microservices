package com.cardio.auth.dto;

public record UserSummaryDTO(
        Long id,
        String email,
        String role,
        boolean enabled
) {
}

