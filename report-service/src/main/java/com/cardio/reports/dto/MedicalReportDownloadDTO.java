package com.cardio.reports.dto;

public class MedicalReportDownloadDTO {

    private String fileName;
    private String contentType;
    private byte[] fileData;

    public MedicalReportDownloadDTO() {
    }

    public MedicalReportDownloadDTO(
            String fileName,
            String contentType,
            byte[] fileData) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
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

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}

