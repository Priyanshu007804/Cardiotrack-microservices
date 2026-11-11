package com.cardio.patient.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegisteredEvent {

    private String eventId;
    private String eventType;
    private Long patientId;
    private Instant timestamp;
}
