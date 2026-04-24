package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Liquid extends Medicine {
    private final String volume;
    private final LocalDateTime lastUpdated;

    // CONSTRUCTOR A: For Database Retrieval
    public Liquid(int id, String name, String brand, Category cat, int stock,
                  BigDecimal price, String volume, LocalDateTime lastUpdated) {
        super(id, name, brand, cat, stock, price);

        this.volume = validateVolume(volume);
        this.lastUpdated = lastUpdated;
    }

    // CONSTRUCTOR B: For New Liquid Creation.
    public Liquid(String name, String brand, Category cat, int stock,
                  BigDecimal price, String volume) {
        this(0, name, brand, cat, stock, price, volume, LocalDateTime.now());
    }

    // Validation
    private static String validateVolume(String volume) {
        String cleanVolume = ValidationUtil.requireNonBlank(volume, "Volume (e.g., 100ml)");

        if (cleanVolume.length() > 50) {
            throw new IllegalArgumentException("Volume description too long (max 50 chars).");
        }
        return cleanVolume;
    }

    // Getters
    public String getVolume() { return volume; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // toString
    @Override
    public String toString() {
        return "Liquid [" + super.toString() + ", Volume='" + volume + "']";
    }
}