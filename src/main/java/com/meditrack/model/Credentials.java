package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Handles security data for the system.
 * Separated from the User profile for better security and auditability.
 */
public class Credentials {
    private final int credentialsId;
    private final int userId;
    private final String hashedPassword;
    private final String ipAddress;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // CONSTRUCTOR A: For Database Retrieval.

    public Credentials(int credentialsId, int userId, String hashedPassword,
                       String ipAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.credentialsId = ValidationUtil.validateNonNegativeInt(credentialsId, "Credentials ID");
        this.userId = ValidationUtil.validatePositiveInt(userId, "User ID");

        this.hashedPassword = ValidationUtil.requireNonBlank(hashedPassword, "Hashed Password");
        this.ipAddress = ValidationUtil.requireNonBlank(ipAddress, "IP Address");

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // CONSTRUCTOR B: For New Credentials.

    public Credentials(int userId, String plainPassword, String ipAddress) {
        this(0, userId, validateAndHash(plainPassword), ipAddress,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // Security Logic

    private static String validateAndHash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        if (!password.matches(passwordRegex)) {
            throw new IllegalArgumentException("Password must contain at least 8 characters, one letter, and one symbol.");
        }

        return hashPassword(password);
    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Security Error: Could not hash password.", ex);
        }
    }

    // Getters
    public int getCredentialsId() { return credentialsId; }
    public int getUserId() { return userId; }
    public String getHashedPassword() { return hashedPassword; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // toString
    @Override
    public String toString() {
        return "Credentials [" +
                "CredID=" + credentialsId +
                ", UserID=" + userId +
                ", IP='" + ipAddress + '\'' +
                ']';
    }
}