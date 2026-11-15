package com.cardio.hospital.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.cardio.hospital.model.CareLogCategory;
import com.cardio.hospital.model.PatientCareLog;

public class CareLogResponseDTO {

    private Long id;
    private Long patientId;
    private Long hospitalStayId;
    private LocalDate logDate;
    private LocalTime logTime;
    private CareLogCategory category;
    private String title;
    private String description;
    private String recordedBy;
    private String recordedByRole;
    private String status;
    private LocalDateTime createdAt;

    public CareLogResponseDTO() {
    }

    public CareLogResponseDTO(
            Long id,
            Long patientId,
            Long hospitalStayId,
            LocalDate logDate,
            LocalTime logTime,
            CareLogCategory category,
            String title,
            String description,
            String recordedBy,
            String recordedByRole,
            String status,
            LocalDateTime createdAt) {
        this.id = id;
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
        this.createdAt = createdAt;
    }

    public static CareLogResponseDTO fromEntity(PatientCareLog entity) {
        if (entity == null) return null;
        return new CareLogResponseDTO(
                entity.getId(),
                entity.getPatientId(),
                entity.getHospitalStayId(),
                entity.getLogDate(),
                entity.getLogTime(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRecordedBy(),
                entity.getRecordedByRole(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
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
