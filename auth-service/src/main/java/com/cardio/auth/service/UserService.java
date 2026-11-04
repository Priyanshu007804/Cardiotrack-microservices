package com.cardio.auth.service;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cardio.auth.dto.AuthResponseDTO;
import com.cardio.auth.dto.LoginRequestDTO;
import com.cardio.auth.dto.RegisterRequestDTO;
import com.cardio.auth.model.Role;
import com.cardio.auth.model.User;
import com.cardio.auth.repository.UserRepository;
import com.cardio.auth.service.JwtService;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO registerUser(RegisterRequestDTO requestDTO) {

        String email = requestDTO.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already registered.");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(Role.PATIENT);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new AuthResponseDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Registration successful",
                null);
    }

    public AuthResponseDTO loginUser(LoginRequestDTO requestDTO) {

        String email = requestDTO.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                requestDTO.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name());
                
        return new AuthResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Login successful",
                token);
    }

    public AuthResponseDTO createDoctorAccount(RegisterRequestDTO requestDTO) {

        String email = requestDTO.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Email is already registered.");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(Role.DOCTOR);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new AuthResponseDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Doctor account created successfully",
                null);
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}