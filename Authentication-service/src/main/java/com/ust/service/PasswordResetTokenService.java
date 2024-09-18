package com.ust.service;

import com.ust.model.PasswordResetToken;
import com.ust.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // Generate and save a new password reset token
    public String createToken(Long employeeId) {
        // Generate a new token
        String token = UUID.randomUUID().toString();

        // Create and save the token entity
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setEmployeeId(employeeId);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour

        passwordResetTokenRepository.save(passwordResetToken);

        return token;
    }

    // Verify if the token is valid and not expired
    public PasswordResetToken verifyToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Check if the token has expired
        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new RuntimeException("Token has expired");
        }

        return passwordResetToken;
    }

    // Delete the used token
    public void deleteToken(String token) {
        passwordResetTokenRepository.deleteByToken(token);
    }
}

