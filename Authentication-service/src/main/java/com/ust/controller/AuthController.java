package com.ust.controller;

import com.ust.dto.JwtResponse;
import com.ust.dto.LoginRequest;
import com.ust.dto.ForgotPasswordRequest;
import com.ust.dto.ResetPasswordRequest;
import com.ust.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    // Endpoint to handle forgot password requests
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.processForgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("Password reset email sent.");
    }

    // Endpoint to handle password reset
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}

