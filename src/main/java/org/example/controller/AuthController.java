package org.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.example.security.JwtTokenProvider;
import org.example.dto.request.LoginRequest;
import org.example.dto.response.AuthResponse;
import org.example.dto.request.UserRegistrationRequest;

import javax.validation.Valid;

import org.example.enums.Role;
import org.example.exception.BadRequestException;
import org.example.model.User;
import org.example.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register/patient")
    public ResponseEntity<User> registerPatient(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        User registeredUser = userService.registerUser(registrationRequest, Role.HASTA);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<User> registerDoctor(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        // Specialty alanının varlığını burada kontrol edebiliriz
        if (registrationRequest.getSpecialty() == null || registrationRequest.getSpecialty().trim().isEmpty()) {
            throw new BadRequestException("Doktor kaydı için uzmanlık alanı gereklidir.");
        }
        User registeredUser = userService.registerUser(registrationRequest, Role.DOKTOR);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}