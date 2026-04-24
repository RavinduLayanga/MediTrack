package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a specific physical batch of medicine.
 * Handles stock levels and expiry tracking at the batch level.
 */
public class Batch {
    private final int medicineId;
    private final String batchNumber;
    private final int stockQuantity;
    private final LocalDate expiryDate;

   // Constructor
    public Batch(int medicineId, String batchNumber, int stockQuantity, LocalDate expiryDate) {
        this.medicineId = ValidationUtil.validatePositiveInt(medicineId, "Medicine ID");
        this.batchNumber = validateBatchNumber(batchNumber);
        this.stockQuantity = ValidationUtil.validateNonNegativeInt(stockQuantity, "Batch Stock");
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date is mandatory.");
    }

    // Business Logic Methods

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        LocalDate threeMonthsFromNow = LocalDate.now().plusMonths(3);
        return !isExpired() && expiryDate.isBefore(threeMonthsFromNow);
    }

    // Validation

    private static String validateBatchNumber(String bNumber) {
        String clean = ValidationUtil.requireNonBlank(bNumber, "Batch Number").toUpperCase();

        if (clean.length() > 50) {
            throw new IllegalArgumentException("Batch number exceeds 50 characters.");
        }
        return clean;
    }

    // Getters
    public int getMedicineId() { return medicineId; }
    public String getBatchNumber() { return batchNumber; }
    public int getStockQuantity() { return stockQuantity; }
    public LocalDate getExpiryDate() { return expiryDate; }

    // toString
    @Override
    public String toString() {
        String status = isExpired() ? " [EXPIRED!]" : (isExpiringSoon() ? " [Expiring Soon]" : "");

        return "Batch [" +
                "MedID=" + medicineId +
                ", BatchNo='" + batchNumber + '\'' +
                ", Stock=" + stockQuantity +
                ", Expiry=" + expiryDate + status +
                ']';
    }
}