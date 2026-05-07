package com.meditrack.service;

import com.meditrack.dao.*;
import com.meditrack.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SaleService {

    private final SaleDAO saleDAO;
    private final MedicineService medicineService;
    private final AuditLogService auditLogService;
    private final BatchDAO batchDAO;


    public SaleService(SaleDAO saleDAO, MedicineService medicineService,
                       AuditLogService auditLogService, BatchDAO batchDAO) {
        this.saleDAO = saleDAO;
        this.medicineService = medicineService;
        this.auditLogService = auditLogService;
        this.batchDAO = batchDAO;
    }

    public Sale processTransaction(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Medicine med = medicineService.getMedicineById(item.getMedicineId());
            if (med == null) {
                System.out.println("[Error] Transaction Failed: Medicine ID " + item.getMedicineId() + " not found.");
                return null;
            }
            if (med.getStock() < item.getQuantity()) {
                System.out.println("[Error] Stock Out: Cannot sell " + item.getQuantity() + " units of " + med.getName() + ". Only " + med.getStock() + " left in inventory.");
                return null;
            }
        }

        Sale processedSale = saleDAO.processSale(sale);

        if (processedSale != null) {
            for (SaleItem item : processedSale.getItems()) {
                medicineService.deductStock(item.getMedicineId(), item.getQuantity(), processedSale.getUserId());
            }

            try {
                auditLogService.recordAction(
                        processedSale.getUserId(),
                        "TRANSACTION_COMPLETE",
                        "sale",
                        null,
                        "SaleID: " + processedSale.getSaleId() + " | Total: LKR " + processedSale.getTotal()
                );
            } catch (Exception e) {
                System.err.println("[Warning] Sale completed, but failed to write to Audit Log.");
            }
            System.out.println("[Success] Transaction completed! Receipt ID: " + processedSale.getSaleId());
        } else {
            System.out.println("[Error] Database failed to process the sale.");
        }
        return processedSale;
    }

    public BigDecimal getDailyRevenue(LocalDate date) {
        List<Sale> sales = saleDAO.getAllSalesByDateRange(date, date);
        return sales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}