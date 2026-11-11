package com.cardio.clinical.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medical_tests")
@Getter
@Setter
public class MedicalTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "hospital_stay_id")
    private Long hospitalStayId;

    @NotBlank(message = "Test name is required")
    @Size(max = 150, message = "Test name cannot exceed 150 characters")
    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @NotBlank(message = "Test type is required")
    @Size(max = 100, message = "Test type cannot exceed 100 characters")
    @Column(name = "test_type", nullable = false, length = 100)
    private String testType;

    @NotNull(message = "Test date is required")
    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;

    @Size(max = 5000, message = "Results cannot exceed 5000 characters")
    @Column(length = 5000)
    private String results;

    @Size(max = 5000, message = "Interpretation cannot exceed 5000 characters")
    @Column(length = 5000)
    private String interpretation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

