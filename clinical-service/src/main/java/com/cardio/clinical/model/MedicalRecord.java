package com.cardio.clinical.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 200, message = "Diagnosis cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String diagnosis;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    @Column(length = 2000)
    private String notes;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
        this.updatedAt = this.recordedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

