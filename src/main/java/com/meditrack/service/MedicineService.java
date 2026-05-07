package com.meditrack.service;

import com.meditrack.dao.BatchDAO;
import com.meditrack.dao.CategoryDAO;
import com.meditrack.dao.MedicineDAO;
import com.meditrack.dao.MedicineSupplierDAO;
import com.meditrack.model.*;
import com.meditrack.service.AuditLogService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MedicineService {

    private final MedicineDAO medicineDAO;
    private final BatchDAO batchDAO;
    private final CategoryDAO categoryDAO;
    private final MedicineSupplierDAO medicineSupplierDAO;
    private final AuditLogService auditLogService;


    private Map<Integer, Medicine> medicineCache = new HashMap<>();

    public MedicineService(MedicineDAO medicineDAO, BatchDAO batchDAO,
                           CategoryDAO categoryDAO, MedicineSupplierDAO medicineSupplierDAO, AuditLogService auditLogService) {
        this.medicineDAO = medicineDAO;
        this.batchDAO = batchDAO;
        this.categoryDAO = categoryDAO;
        this.medicineSupplierDAO = medicineSupplierDAO;
        this.auditLogService = auditLogService;
        refreshCache();
    }

    public void refreshCache() {
        medicineCache.clear();
        List<Medicine> allMeds = medicineDAO.getAllMedicines();
        for (Medicine med : allMeds) {
            medicineCache.put(med.getMedicineId(), med);
        }
    }

    public Medicine getMedicineById(int id) {
        return medicineCache.get(id);
    }

    public List<Medicine> searchMedicines(String partialName) {
        String query = partialName.toLowerCase();

        return medicineCache.values().stream()
                .filter(m -> m.getName().toLowerCase().contains(query) ||
                        m.getBrand().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    public List<Medicine> listAllInventory() {
        return medicineCache.values().stream().collect(Collectors.toList());
    }


    public Medicine addMedicine(Medicine medicine, int performedByUserId) {
        if (categoryDAO.getCategoryById(medicine.getCategory().getCategoryId()) == null) {
            System.out.println("[Error] Validation Failed: The specified category does not exist.");
            return null;
        }

        boolean exists = medicineCache.values().stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(medicine.getName()) &&
                        m.getBrand().equalsIgnoreCase(medicine.getBrand()));

        if (exists) {
            System.out.println("[Error] A medicine with this generic name and brand already exists.");
            return null;
        }

        Medicine savedMedicine = medicineDAO.saveMedicine(medicine);
        if (savedMedicine != null) {
            System.out.println("[Success] Medicine added successfully: " + savedMedicine.getName());

            // Add to the logs
            auditLogService.recordAction(
                    performedByUserId,
                    "ADD_MEDICINE",
                    "medicine",
                    null,
                    "New item added to catalog: " + savedMedicine.getName() + " (" + savedMedicine.getBrand() + ")"
            );


            refreshCache();
        }
        return savedMedicine;
    }

    public void deductStock(int medicineId, int quantitySold, int performedByUserId) {
        Medicine med = getMedicineById(medicineId);
        if (med != null) {
            int oldStock = med.getStock();
            int newStock = med.getStock() - quantitySold;

            Medicine updatedMed;
            if (med instanceof Tablet) {
                Tablet t = (Tablet) med;
                updatedMed = new Tablet(t.getMedicineId(), t.getName(), t.getBrand(),
                        t.getCategory(), newStock, t.getPrice(), t.getDosage(), t.getLastUpdated());
            } else {
                Liquid l = (Liquid) med;
                updatedMed = new Liquid(l.getMedicineId(), l.getName(), l.getBrand(),
                        l.getCategory(), newStock, l.getPrice(), l.getVolume(), l.getLastUpdated());
            }
            medicineDAO.updateMedicine(updatedMed);

            // Log update
            auditLogService.recordAction(
                    performedByUserId,
                    "STOCK_DEDUCTION",
                    "medicine",
                    "Qty: " + oldStock,
                    "Sold: " + quantitySold + " | Remaining: " + newStock
            );

            refreshCache();
        }
    }

    public boolean receiveNewBatch(Batch batch, int performedByUserId) {
        Medicine existingMed = getMedicineById(batch.getMedicineId());
        if (existingMed == null) {
            System.out.println("[Error] Cannot receive batch. Medicine ID " + batch.getMedicineId() + " not found.");
            return false;
        }
        batchDAO.saveBatch(batch);

        int oldStock = existingMed.getStock();
        int newTotalStock = existingMed.getStock() + batch.getStockQuantity();

        Medicine updatedMed;
        if (existingMed instanceof Tablet) {
            Tablet t = (Tablet) existingMed;
            updatedMed = new Tablet(t.getMedicineId(), t.getName(), t.getBrand(),
                    t.getCategory(), newTotalStock, t.getPrice(),
                    t.getDosage(), t.getLastUpdated());
        } else {
            Liquid l = (Liquid) existingMed;
            updatedMed = new Liquid(l.getMedicineId(), l.getName(), l.getBrand(),
                    l.getCategory(), newTotalStock, l.getPrice(),
                    l.getVolume(), l.getLastUpdated());
        }

        medicineDAO.updateMedicine(updatedMed);

        // LOG update
        auditLogService.recordAction(
                performedByUserId,
                "RECEIVE_BATCH",
                "batch",
                "Initial: " + oldStock,
                "Added: " + batch.getStockQuantity() + " | Total: " + newTotalStock + " | Med: " + existingMed.getName()
        );


        System.out.println("[Success] Batch logged. Stock for " + existingMed.getName() + " updated to " + newTotalStock);

        refreshCache();
        return true;
    }

    public List<Medicine> getLowStockAlerts(int threshold) {
        return medicineCache.values().stream()
                .filter(m -> m.getStock() < threshold)
                .collect(Collectors.toList());
    }

    public List<Batch> getExpiringInventory() {
        return batchDAO.getAllBatches().stream()
                .filter(b -> b.isExpired() || b.isExpiringSoon())
                .collect(Collectors.toList());
    }

    public boolean linkMedicineToSupplier(int supplierId, int medicineId, BigDecimal supplyPrice, int leadTime) {
        MedicineSupplier link = new MedicineSupplier(supplierId, medicineId, supplyPrice, leadTime);

        if (medicineSupplierDAO.saveLink(link) != null) {
            System.out.println("[Success] Supplier linked to medicine successfully.");
            return true;
        } else {
            System.out.println("[Error] Failed to link supplier to medicine. Please verify IDs exist.");
            return false;
        }
    }
}