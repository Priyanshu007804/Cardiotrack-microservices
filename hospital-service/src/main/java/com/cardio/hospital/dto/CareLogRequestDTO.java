package com.cardio.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cardio.hospital.model.CareLogCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CareLogRequestDTO {

    private Long hospitalStayId;

    private LocalDate logDate;

    private LocalTime logTime;

    @NotNull(message = "Category is required")
    private CareLogCategory category;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 120, message = "Recorded by cannot exceed 120 characters")
    private String recordedBy;

    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;

    public CareLogRequestDTO() {
    }

    public CareLogRequestDTO(
            Long hospitalStayId,
            LocalDate logDate,
            LocalTime logTime,
            CareLogCategory category,
            String title,
            String description,
            String recordedBy,
            String status) {
        this.hospitalStayId = hospitalStayId;
        this.logDate = logDate;
        this.logTime = logTime;
        this.category = category;
        this.title = title;
        this.description = description;
        this.recordedBy = recordedBy;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
