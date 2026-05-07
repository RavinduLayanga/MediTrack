package com.meditrack.service;

import com.meditrack.dao.MedicineSupplierDAO;
import com.meditrack.dao.SupplierDAO;
import com.meditrack.model.MedicineSupplier;
import com.meditrack.model.Supplier;

import java.math.BigDecimal;
import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO;
    private final MedicineSupplierDAO medicineSupplierDAO;
    private final MedicineService medicineService;

    public SupplierService(SupplierDAO supplierDAO, MedicineSupplierDAO medicineSupplierDAO, MedicineService medicineService) {
        this.supplierDAO = supplierDAO;
        this.medicineSupplierDAO = medicineSupplierDAO;
        this.medicineService = medicineService;
    }


    public Supplier addSupplier(Supplier supplier) {
        boolean exists = supplierDAO.getAllSuppliers().stream()
                .anyMatch(s -> s.getEmail().equalsIgnoreCase(supplier.getEmail()) ||
                        s.getName().equalsIgnoreCase(supplier.getName()));

        if (exists) {
            System.out.println("[Error] A supplier with this Name or Email already exists.");
            return null;
        }

        Supplier saved = supplierDAO.saveSupplier(supplier);
        if (saved != null) {
            System.out.println("[Success] Supplier added successfully: " + saved.getName());
        } else {
            System.out.println("[Error] Database failed to save the supplier.");
        }
        return saved;
    }

    public Supplier updateSupplier(Supplier supplier) {
        boolean emailClash = supplierDAO.getAllSuppliers().stream()
                .anyMatch(s -> s.getEmail().equalsIgnoreCase(supplier.getEmail()) &&
                        s.getSupplierId() != supplier.getSupplierId());

        if (emailClash) {
            System.out.println("[Error] Update Failed: The email '" + supplier.getEmail() + "' is already used by another supplier.");
            return null;
        }

        Supplier updated = supplierDAO.updateSupplier(supplier);
        if (updated != null) {
            System.out.println("[Success] Supplier updated successfully.");
        } else {
            System.out.println("[Error] Failed to update supplier in database.");
        }
        return updated;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
    }

    public Supplier getSupplierById(int id) {
        Supplier supplier = supplierDAO.getSupplierById(id);
        if (supplier == null) {
            System.out.println("[Error] No supplier found with ID: " + id);
        }
        return supplier;
    }

    public boolean removeSupplier(int supplierId) {
        if (supplierDAO.getSupplierById(supplierId) == null) {
            System.out.println("[Error] Delete Failed: Supplier ID " + supplierId + " does not exist.");
            return false;
        }

        boolean success = supplierDAO.deleteSupplier(supplierId);
        if (success) {
            System.out.println("[Success] Supplier deleted successfully.");
        } else {
            System.out.println("[Error] Failed to delete supplier from database.");
        }
        return success;
    }

    public boolean linkMedicineToSupplier(int supplierId, int medicineId, BigDecimal price, int leadTime) {
        if (supplierDAO.getSupplierById(supplierId) == null) {
            System.out.println("[Error] Link Failed: Supplier ID " + supplierId + " not found.");
            return false;
        }
        if (medicineService.getMedicineById(medicineId) == null) {
            System.out.println("[Error] Link Failed: Medicine ID " + medicineId + " not found.");
            return false;
        }

        MedicineSupplier link = new MedicineSupplier(supplierId, medicineId, price, leadTime);

        if (medicineSupplierDAO.saveLink(link) != null) {
            System.out.println("[Success] Successfully linked Medicine ID " + medicineId + " to Supplier ID " + supplierId + ".");
            return true;
        } else {
            System.out.println("[Error] Failed to save the supplier link to the database.");
            return false;
        }
    }

    public List<MedicineSupplier> getSuppliersForMedicine(int medicineId) {
        return medicineSupplierDAO.getSuppliersByMedicineId(medicineId);
    }
}