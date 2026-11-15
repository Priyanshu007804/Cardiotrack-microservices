package com.cardio.doctor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cardio.doctor.dto.DoctorRequestDTO;
import com.cardio.doctor.dto.DoctorResponseDTO;
import com.cardio.doctor.dto.DoctorUpdateRequestDTO;
import com.cardio.doctor.service.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @PostMapping("/me/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponseDTO createMyProfile(
            @Valid @RequestBody DoctorRequestDTO request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return doctorService.createDoctor(userId, request);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponseDTO createDoctorProfile(
            @RequestParam Long userId,
            @Valid @RequestBody DoctorRequestDTO request) {

        return doctorService.createDoctor(userId, request);
    }

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDTO getMyProfile(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return doctorService.getDoctorByUserId(userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    public DoctorResponseDTO getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponseDTO updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequestDTO request) {
        return doctorService.updateDoctor(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDTO updateMyProfile(
            @Valid @RequestBody DoctorUpdateRequestDTO request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        DoctorResponseDTO doctor = doctorService.getDoctorByUserId(userId);

        return doctorService.updateDoctor(doctor.id(), request);
    }

    @PostMapping("/{id}/photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public DoctorResponseDTO uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return doctorService.uploadProfilePicture(id, file);
    }

    @PostMapping("/me/photo")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponseDTO uploadMyPhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DoctorResponseDTO doctor = doctorService.getDoctorByUserId(userId);
        return doctorService.uploadProfilePicture(doctor.id(), file);
    }

    @PostMapping("/photo/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    public Map<String, String> uploadPhotoOnly(
            @RequestParam("file") MultipartFile file) {
        String url = doctorService.uploadPhotoOnly(file);
        return Map.of("url", url);
    }
}
