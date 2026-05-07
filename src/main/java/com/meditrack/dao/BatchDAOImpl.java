package com.meditrack.dao;

import com.meditrack.model.Batch;
import com.meditrack.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BatchDAOImpl implements BatchDAO {
    private static final String SELECT_COLUMNS= "SELECT medicine_id, batch_number, stock_quantity, expiry_date ";

    @Override
    public Batch saveBatch(Batch batch) {
        String sql = "INSERT INTO batches (medicine_id, batch_number, stock_quantity, expiry_date) VALUES (?, ?, ?, ?)";
        int affectedRows = executeModification(sql,
                batch.getMedicineId(),
                batch.getBatchNumber(),
                batch.getStockQuantity(),
                batch.getExpiryDate()
        );
        return affectedRows > 0 ? batch : null;
    }

    @Override
    public Batch updateBatchStock(int medicineId, String batchNumber, int newQuantity) {
        String sql = "UPDATE batches SET stock_quantity ? WHERE medicine_id = ? AND batch_number = ?";
        int affectedRows =executeModification(sql,newQuantity,medicineId,batchNumber);

        if (affectedRows>0){
            return getBatchesByCompositeKey(medicineId, batchNumber);
        }
        return null;
    }

    @Override
    public List<Batch> getAllBatches() {
        String sql = SELECT_COLUMNS + "FROM batches ORDER BY expiry_date ASC";
        return fetchBatches(sql);
    }

    @Override
    public List<Batch> getAllBatchesByMedicineId(int medicineId) {
        String sql = SELECT_COLUMNS + "FROM batches WHERE medicine_id = ? ORDER BY expiry_date ASC";
        return fetchBatches(sql,medicineId);
    }

    @Override
    public boolean deleteBatch(int medicineId, String batchNumber) {
        String sql = "DELETE FROM batches WHERE medicine_id  = ? AND batch_number = ?";
        return executeModification(sql,medicineId,batchNumber) > 0;
    }

    private Batch getBatchesByCompositeKey(int medicineId, String batchNumber) {
        String sql = SELECT_COLUMNS + "FROM batches WHERE medicine_id = ? AND batch_number = ?";
        List<Batch> results = fetchBatches(sql, medicineId, batchNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    // For Modifying Data (INSERT, UPDATE, DELETE)
    private int executeModification(String sql, Object...params) {
        try{
            Connection conn = DBConnection.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                for(int i=0; i < params.length; i++){
                    stmt.setObject(i+1,params[i]);
                }
                return stmt.executeUpdate();

            }
        }catch (Exception e){
            System.err.println("[Database Modification Failed] " + e.getMessage());
        }
        return 0;
    }

    private List<Batch> fetchBatches(String sql, Object...parms){
        List<Batch> batches = new ArrayList<>();
        try{
            Connection conn = DBConnection.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                for(int i = 0; i < parms.length; i++){
                    stmt.setObject(i+1, parms[i]);
                }
                try(ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        batches.add(new Batch(
                                rs.getInt("medicine_id"),
                                rs.getString("batch_number"),
                                rs.getInt("stock_quantity"),
                                rs.getDate("expiry_date").toLocalDate()
                        ));
                    }
                }
            }
        } catch (Exception e){
            System.err.println("[Database Error] Could not fetch batches: " + e.getMessage());
        }
        return batches;
    }
}
