package com.meditrack.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private final int saleId;
    private final int userId;
    private final List<SaleItem> items;
    private final BigDecimal total;
    private final LocalDateTime purchaseDate;

    // Constructor A: For Database Retrieval
    public Sale(int saleId, int userId, List<SaleItem> items, BigDecimal total, LocalDateTime purchaseDate) {
        this.saleId = saleId;
        this.userId = validateUserId(userId);
        this.items = List.copyOf(validateItems(items));
        this.total = total.setScale(2, RoundingMode.HALF_UP);
        this.purchaseDate = purchaseDate;
    }

    // Constructor B: For New Sale Creation
    public Sale(int userId, List<SaleItem> items) {
        this(0, userId, items, calculateTotal(items), LocalDateTime.now());
    }

    //  Logic & Validations

    private int validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid User ID for this sale.");
        }
        return userId;
    }

    private static List<SaleItem> validateItems(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sale must have at least one item.");
        }
        return items;
    }

    private static BigDecimal calculateTotal(List<SaleItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleItem item : items) {
            sum = sum.add(item.getSubtotal());
        }
        return sum;
    }

    // Getters
    public int getSaleId() { return saleId; }
    public int getUserId() { return userId; }
    public List<SaleItem> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }

    // toString
    @Override
    public String toString() {
        return "Sale [" +
                "SaleID=" + saleId +
                ", CashierID=" + userId +
                ", ItemsCount=" + items.size() +
                ", Total=LKR " + total +
                ", Date=" + purchaseDate.toLocalDate() +
                ']';
    }
}