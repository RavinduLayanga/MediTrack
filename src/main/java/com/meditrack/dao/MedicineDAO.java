package com.meditrack.dao;

import com.meditrack.model.Medicine;
import java.util.List;

public interface MedicineDAO {
    Medicine saveMedicine(Medicine medicine);
    Medicine getMedicineById(int id);
    Medicine updateMedicine(Medicine medicine);
    List<Medicine> getAllMedicines();
    boolean deleteMedicine(int id);
}
