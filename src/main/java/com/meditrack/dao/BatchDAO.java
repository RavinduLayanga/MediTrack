package com.meditrack.dao;

import com.meditrack.model.Batch;
import java.util.List;

public interface BatchDAO {
    Batch saveBatch(Batch batch);
    Batch updateBatchStock(int medicineId, String batchNumber,int newQuantity);
    List<Batch> getAllBatches();
    List<Batch> getAllBatchesByMedicineId(int medicineId);
    boolean deleteBatch(int medicineId, String batchNumber);
}
