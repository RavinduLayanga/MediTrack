package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Tablet extends Medicine {
    private final String dosage;
    private final LocalDateTime lastUpdated;

    // CONSTRUCTOR A: For Database Retrieval
    public Tablet(int id, String name, String brand, Category cat, int stock,
                  BigDecimal price, String dosage, LocalDateTime lastUpdated) {
        super(id, name, brand, cat, stock, price);

        this.dosage = validateDosage(dosage);
        this.lastUpdated = lastUpdated;
    }

    // CONSTRUCTOR B: For New Tablet Creation.
    public Tablet(String name, String brand, Category cat, int stock,
                  BigDecimal price, String dosage) {
        this(0, name, brand, cat, stock, price, dosage, LocalDateTime.now());
    }

    // Validation Logic

    private String validateDosage(String dosage) {
        String cleanDosage = ValidationUtil.requireNonBlank(dosage, "Dosage (e.g., 500mg)");

        if (cleanDosage.length() > 50) {
            throw new IllegalArgumentException("Dosage description too long (max 50 chars).");
        }
        return cleanDosage;
    }

    // Getters
    public String getDosage() { return dosage; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // toString
    @Override
    public String toString() {
        return "Tablet [" + super.toString() + ", Dosage='" + dosage + "']";
    }
}