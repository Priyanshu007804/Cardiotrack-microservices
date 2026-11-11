package com.cardio.hospital.model;

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
@Table(name = "hospital_stays")
public class HospitalStay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cross-service scalar reference to Patient Service
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Column(name = "admission_reason", nullable = false, length = 255)
    private String admissionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private HospitalStayStatus status = HospitalStayStatus.ADMITTED;

    @Column(name = "notes", length = 2000)
    private String notes;

    public HospitalStay() {
    }

    public HospitalStay(
            Long patientId,
            LocalDateTime admissionDate,
            LocalDateTime dischargeDate,
            String admissionReason,
            HospitalStayStatus status,
            String notes) {
        this.patientId = patientId;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.admissionReason = admissionReason;
        this.status = status;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (this.admissionDate == null) {
            this.admissionDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = HospitalStayStatus.ADMITTED;
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

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getAdmissionReason() {
        return admissionReason;
    }

    public void setAdmissionReason(String admissionReason) {
        this.admissionReason = admissionReason;
    }

    public HospitalStayStatus getStatus() {
        return status;
    }

    public void setStatus(HospitalStayStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

