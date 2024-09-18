package com.ust.service;

import com.ust.client.EmployeeClient;
import com.ust.dto.EmployeeDto;
import com.ust.dto.JwtResponse;
import com.ust.dto.LoginRequest;
import com.ust.model.PasswordResetToken;
import com.ust.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmployeeClient employeeClient;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Authenticate user and generate JWT
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        return new JwtResponse(jwt);
    }

    // Generate password reset token and send email
    public void processForgotPassword(String email) {
        // Generate a new token
        String token = passwordResetTokenService.createToken(employeeClient.getEmployeeByEmail(email).getId());

        // Send password reset email
        emailService.sendPasswordResetEmail(email, token);
    }

    // Verify token and update password
    public void resetPassword(String token, String newPassword) {
        // Verify the token
        PasswordResetToken passwordResetToken = passwordResetTokenService.verifyToken(token);

        // Retrieve the employee details
        Long employeeId = passwordResetToken.getEmployeeId();
        EmployeeDto employee = employeeClient.getEmployeeById(employeeId);

        // Encode new password
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Update the employee password
        employeeClient.updateEmployeePassword(employeeId, encodedPassword);

        // Delete the used token
        passwordResetTokenService.deleteToken(token);
    }
}
