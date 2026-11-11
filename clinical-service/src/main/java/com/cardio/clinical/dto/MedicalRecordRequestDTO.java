package com.cardio.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MedicalRecordRequestDTO {

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 200, message = "Diagnosis cannot exceed 200 characters")
    private String diagnosis;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    private Long doctorId;

    public MedicalRecordRequestDTO() {
    }

    public MedicalRecordRequestDTO(String diagnosis, String notes, Long doctorId) {
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.doctorId = doctorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}

