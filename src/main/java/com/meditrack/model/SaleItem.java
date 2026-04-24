package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleItem {
    private final int saleItemId;
    private final int medicineId;
    private final int quantity;
    private final BigDecimal unitPriceAtSale;

    // Constructor A: For Database Retrieval
    public SaleItem(int saleItemId, int medicineId, int quantity, BigDecimal unitPriceAtSale) {
        this.saleItemId = saleItemId;
        this.medicineId = ValidationUtil.validatePositiveInt(medicineId, "Medicine ID");
        this.quantity = ValidationUtil.validatePositiveInt(quantity, "Quantity");
        this.unitPriceAtSale = ValidationUtil.validatePrice(unitPriceAtSale, "Unit Price");
    }

    // Constructor B: For New Sale Items
    public SaleItem(int medicineId, int quantity, BigDecimal unitPriceAtSale) {
        this(0, medicineId, quantity, unitPriceAtSale);
    }

    // Business Logic
    public BigDecimal getSubtotal() {
        return unitPriceAtSale
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters
    public int getSaleItemId() { return saleItemId; }
    public int getMedicineId() { return medicineId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPriceAtSale() { return unitPriceAtSale; }

    // toString
    @Override
    public String toString() {
        return "SaleItem [" +
                "MedicineID=" + medicineId +
                ", Qty=" + quantity +
                ", UnitPrice=LKR " + unitPriceAtSale +
                ", Subtotal=LKR " + getSubtotal() +
                ']';
    }
}