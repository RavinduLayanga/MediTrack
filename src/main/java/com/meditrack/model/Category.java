package com.meditrack.model;

import com.meditrack.util.ValidationUtil;

/**
 * Represents a medicine category (e.g., Antibiotics, Vitamins).
 * Ensures data integrity through strict constructor validation.
 */
public class Category {
    private final int categoryId;
    private final String categoryName;

    // CONSTRUCTOR A: For Database Retrieval
    public Category(int categoryId, String categoryName) {
        this.categoryId = ValidationUtil.validateNonNegativeInt(categoryId, "Category ID");
        this.categoryName = validateCategoryName(categoryName);
    }

    // CONSTRUCTOR B: For New Category Creation
    public Category(String categoryName) {
        this(0, categoryName);
    }

    // Validation
    private static String validateCategoryName(String name) {
        String cleanName = ValidationUtil.requireNonBlank(name, "Category Name");

        // Length check
        if (cleanName.length() > 100) {
            throw new IllegalArgumentException("Category name is too long (max 100 characters).");
        }

        // allow only letters, numbers, and basic spaces/hyphens
        if (!cleanName.matches("^[a-zA-Z0-9\\s\\-]+$")) {
            throw new IllegalArgumentException("Category name contains invalid characters.");
        }

        return cleanName;
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    // toString
    @Override
    public String toString() {
        return "Category [" +
                "ID=" + categoryId +
                ", Name='" + categoryName + '\'' +
                ']';
    }
}