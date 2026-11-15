package com.cardio.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "patient_care_logs")
public class PatientCareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "hospital_stay_id")
    private Long hospitalStayId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "log_time", nullable = false)
    private LocalTime logTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private CareLogCategory category = CareLogCategory.GENERAL;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recorded_by", length = 120)
    private String recordedBy;

    @Column(name = "recorded_by_role", length = 50)
    private String recordedByRole;

    @Column(name = "status", length = 50)
    private String status = "COMPLETED";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PatientCareLog() {
    }

    public PatientCareLog(
            Long patientId,
            Long hospitalStayId,
            LocalDate logDate,
            LocalTime logTime,
            CareLogCategory category,
            String title,
            String description,
            String recordedBy,
            String recordedByRole,
            String status) {
        this.patientId = patientId;
        this.hospitalStayId = hospitalStayId;
        this.logDate = logDate;
        this.logTime = logTime;
        this.category = category;
        this.title = title;
        this.description = description;
        this.recordedBy = recordedBy;
        this.recordedByRole = recordedByRole;
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.logDate == null) {
            this.logDate = LocalDate.now();
        }
        if (this.logTime == null) {
            this.logTime = LocalTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "COMPLETED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getHospitalStayId() {
        return hospitalStayId;
    }

    public void setHospitalStayId(Long hospitalStayId) {
        this.hospitalStayId = hospitalStayId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LocalTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalTime logTime) {
        this.logTime = logTime;
    }

    public CareLogCategory getCategory() {
        return category;
    }

    public void setCategory(CareLogCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getRecordedByRole() {
        return recordedByRole;
    }

    public void setRecordedByRole(String recordedByRole) {
        this.recordedByRole = recordedByRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
