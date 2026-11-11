package com.cardio.reports.model;

import java.time.LocalDateTime;

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
@Table(name = "medical_reports")
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cross-service scalar reference to Patient Service
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    // Optional cross-service scalar reference to Clinical Service
    @Column(name = "medical_test_id")
    private Long medicalTestId;

    // Cross-service scalar reference to Auth Service user
    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedicalReportStatus status = MedicalReportStatus.ACTIVE;

    public MedicalReport() {
    }

    public MedicalReport(
            Long patientId,
            Long medicalTestId,
            Long uploadedByUserId,
            String fileName,
            String contentType,
            String storageKey,
            Long fileSizeBytes,
            LocalDateTime uploadedAt,
            MedicalReportStatus status) {
        this.patientId = patientId;
        this.medicalTestId = medicalTestId;
        this.uploadedByUserId = uploadedByUserId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.storageKey = storageKey;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedAt = uploadedAt;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = MedicalReportStatus.ACTIVE;
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

    public Long getMedicalTestId() {
        return medicalTestId;
    }

    public void setMedicalTestId(Long medicalTestId) {
        this.medicalTestId = medicalTestId;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public MedicalReportStatus getStatus() {
        return status;
    }

    public void setStatus(MedicalReportStatus status) {
        this.status = status;
    }
}

