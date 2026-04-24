package com.meditrack.dao;

import com.meditrack.model.MedicineSupplier;

import java.util.List;

public interface MedicineSupplierDAO {
    MedicineSupplier saveLink(MedicineSupplier link);
    List<MedicineSupplier> getSuppliersByMedicineId(int medicineId);
    List<MedicineSupplier> getMedicinesBySupplierId(int supplierId);
    boolean deleteLink(int supplierId, int medicineId);
}
