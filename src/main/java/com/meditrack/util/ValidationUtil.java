package com.meditrack.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ValidationUtil {

    // Null/Blank Checker
    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required and cannot be empty.");
        }
        return value.trim();
    }

    // Email Validation
    public static String validateEmail(String email) {
        String cleanEmail = requireNonBlank(email, "Email");

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!cleanEmail.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        return cleanEmail.toLowerCase();
    }

    // Contact Validation
    public static String validateContact(String contactNo) {
        String nonBlankContact = requireNonBlank(contactNo, "Contact Number");

        // Remove all spaces, hyphens, or brackets
        String cleanContact = nonBlankContact.replaceAll("[\\s\\-\\(\\)]", "");

        /* Sri Lankan Regex
       ^(?:0|\+94|94) -> Starts with 0 OR +94 OR 94
       [1-9]          -> The next digit cannot be 0
       \d{8}          -> Followed by exactly 8 more digits
       */

        String slRegex = "^(?:0|\\+94|94)[1-9]\\d{8}$";
        if (!cleanContact.matches(slRegex)) {
            throw new IllegalArgumentException("Invalid Sri Lankan contact number format.");
        }
        return cleanContact;
    }

    // Validate positive Int
    public static int validatePositiveInt(int val, String fieldName) {
        if (val <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        return val;
    }

    // Price Validation
    public static BigDecimal validatePrice(BigDecimal price, String fieldName) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    // Validate non-negative Int (Allows 0)
    public static int validateNonNegativeInt(int val, String fieldName) {
        if (val < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        return val;
    }
}