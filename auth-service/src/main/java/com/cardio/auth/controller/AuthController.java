package com.cardio.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cardio.auth.dto.AuthResponseDTO;
import com.cardio.auth.dto.LoginRequestDTO;
import com.cardio.auth.dto.RegisterRequestDTO;
import com.cardio.auth.dto.UserSummaryDTO;
import com.cardio.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(
            @Valid @RequestBody RegisterRequestDTO requestDTO) {

        AuthResponseDTO response = userService.registerUser(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid @RequestBody LoginRequestDTO requestDTO) {

        AuthResponseDTO response = userService.loginUser(requestDTO);

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDTO> createDoctorAccount(
            @Valid @RequestBody RegisterRequestDTO requestDTO) {

        AuthResponseDTO response =
                userService.createDoctorAccount(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDTO> createStaffAccount(
            @Valid @RequestBody RegisterRequestDTO requestDTO) {

        AuthResponseDTO response =
                userService.createStaffAccount(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AuthResponseDTO> createPatientAccount(
            @Valid @RequestBody RegisterRequestDTO requestDTO) {

        AuthResponseDTO response =
                userService.createAccountWithRole(requestDTO,
                        com.cardio.auth.model.Role.PATIENT);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestParam(required = false) com.cardio.auth.model.Role role,
            @RequestBody(required = false) Map<String, String> body) {

        com.cardio.auth.model.Role targetRole = role;
        if (targetRole == null && body != null && body.containsKey("role")) {
            targetRole = com.cardio.auth.model.Role.valueOf(body.get("role").toUpperCase());
        }
        if (targetRole == null) {
            throw new IllegalArgumentException("Role parameter or body is required");
        }

        AuthResponseDTO response = userService.updateUserRole(userId, targetRole);
        return ResponseEntity.ok(response);
    }
}