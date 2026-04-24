package com.meditrack.model;

import com.meditrack.util.ValidationUtil;
import java.math.BigDecimal;

/**
 * Tracks which supplier provides which medicine, at what price, and how fast.
 */
public class MedicineSupplier {
    private final int supplierId;
    private final int medicineId;
    private final BigDecimal supplyPrice;
    private final int leadTime;

    public MedicineSupplier(int supplierId, int medicineId, BigDecimal supplyPrice, int leadTime) {
        this.supplierId = ValidationUtil.validatePositiveInt(supplierId, "Supplier ID");
        this.medicineId = ValidationUtil.validatePositiveInt(medicineId, "Medicine ID");
        this.supplyPrice = ValidationUtil.validatePrice(supplyPrice, "Supply Price");
        this.leadTime = ValidationUtil.validateNonNegativeInt(leadTime, "Lead Time");
    }

    // Getters
    public int getSupplierId() { return supplierId; }
    public int getMedicineId() { return medicineId; }
    public BigDecimal getSupplyPrice() { return supplyPrice; }
    public int getLeadTime() { return leadTime; }

    // toString
    @Override
    public String toString() {
        return "MedicineSupplier [" +
                "SupplierID=" + supplierId +
                ", MedicineID=" + medicineId +
                ", SupplyPrice=LKR " + supplyPrice +
                ", LeadTime=" + leadTime + " days" +
                ']';
    }
}