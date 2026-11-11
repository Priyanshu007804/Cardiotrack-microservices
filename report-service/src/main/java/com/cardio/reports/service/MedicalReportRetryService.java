package com.cardio.reports.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cardio.reports.model.MedicalReport;
import com.cardio.reports.model.MedicalReportStatus;
import com.cardio.reports.repository.MedicalReportRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class MedicalReportRetryService {

    private static final Logger log = LoggerFactory.getLogger(MedicalReportRetryService.class);

    private final Cloudinary cloudinary;
    private final MedicalReportRepository medicalReportRepository;
    private final MedicalReportDeletionService medicalReportDeletionService;

    public MedicalReportRetryService(
            Cloudinary cloudinary,
            MedicalReportRepository medicalReportRepository,
            MedicalReportDeletionService medicalReportDeletionService) {
        this.cloudinary = cloudinary;
        this.medicalReportRepository = medicalReportRepository;
        this.medicalReportDeletionService = medicalReportDeletionService;
    }

    // Runs every 5 minutes (300,000 ms)
    @Scheduled(fixedDelay = 300000)
    public void retryPendingDeletions() {
        List<MedicalReport> pendingReports = medicalReportRepository.findByStatus(MedicalReportStatus.PENDING_DELETION);

        if (pendingReports.isEmpty()) {
            return;
        }

        log.info("Found {} pending report deletion(s) to retry", pendingReports.size());

        for (MedicalReport report : pendingReports) {
            try {
                Map<?, ?> result = cloudinary.uploader().destroy(
                        report.getStorageKey(),
                        ObjectUtils.asMap(
                                "resource_type", "raw",
                                "type", "authenticated"
                        )
                );

                String deletionResult = (String) result.get("result");

                if ("ok".equals(deletionResult) || "not found".equals(deletionResult)) {
                    medicalReportDeletionService.updateStatus(report.getId(), MedicalReportStatus.DELETED);
                    log.info("Successfully deleted pending report id: {}", report.getId());
                } else {
                    log.warn("Unexpected Cloudinary deletion result for report id {}: {}", report.getId(), deletionResult);
                }
            } catch (Exception e) {
                log.error("Retry failed for medical report id {}: {}", report.getId(), e.getMessage());
            }
        }
    }
}

