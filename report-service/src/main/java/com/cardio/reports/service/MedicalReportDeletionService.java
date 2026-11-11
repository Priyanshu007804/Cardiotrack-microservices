package com.cardio.reports.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cardio.reports.model.MedicalReport;
import com.cardio.reports.model.MedicalReportStatus;
import com.cardio.reports.repository.MedicalReportRepository;

@Service
public class MedicalReportDeletionService {

    private final MedicalReportRepository medicalReportRepository;

    public MedicalReportDeletionService(MedicalReportRepository medicalReportRepository) {
        this.medicalReportRepository = medicalReportRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long reportId, MedicalReportStatus status) {
        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("Medical report not found with id: " + reportId));

        report.setStatus(status);
        medicalReportRepository.saveAndFlush(report);
    }
}

