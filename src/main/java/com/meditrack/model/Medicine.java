package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Abstract base class for all pharmaceutical products.
 */
public abstract class Medicine {
    protected final int medicineId;
    protected final String name;
    protected final String brand;
    protected final Category category;
    protected final int stock;
    protected final BigDecimal price;

    // CONSTRUCTOR A: For Database Retrieval
    public Medicine(int medicineId, String name, String brand, Category category, int stock, BigDecimal price) {
        this.medicineId = ValidationUtil.validateNonNegativeInt(medicineId, "Medicine ID");
        this.name = validateStringLength(name, "Generic Name", 100);
        this.brand = validateStringLength(brand, "Brand Name", 100);
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
        this.stock = ValidationUtil.validateNonNegativeInt(stock, "Stock");
        this.price = ValidationUtil.validatePrice(price, "Price");
    }

    // CONSTRUCTOR B: For New Medicine Creation.
    public Medicine(String name, String brand, Category category, int stock, BigDecimal price) {
        this(0, name, brand, category, stock, price);
    }

    // Validation
    private static String validateStringLength(String val, String fieldName, int maxLength) {
        String cleanVal = ValidationUtil.requireNonBlank(val, fieldName);

        if (cleanVal.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters.");
        }
        return cleanVal;
    }

    // Getters
    public int getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public Category getCategory() { return category; }
    public int getStock() { return stock; }
    public BigDecimal getPrice() { return price; }

    // toString
    @Override
    public String toString() {
        return "ID=" + medicineId +
                ", Name='" + name + '\'' +
                ", Brand='" + brand + '\'' +
                ", Category=" + category.getCategoryName() +
                ", Stock=" + stock +
                ", Price=LKR " + price;
    }
}