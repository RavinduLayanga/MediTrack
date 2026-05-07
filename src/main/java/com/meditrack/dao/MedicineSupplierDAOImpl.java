package com.meditrack.dao;

import com.meditrack.model.MedicineSupplier;
import com.meditrack.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicineSupplierDAOImpl implements MedicineSupplierDAO {
    private static final String SELECT_COLUMNS = "SELECT supplier_id, medicine_id, supply_price, lead_time ";

    @Override
    public MedicineSupplier saveLink(MedicineSupplier link) {
        String sql = "INSERT INTO medicine_supplier (supplier_id, medicine_id, supply_price, lead_time) VALUES (?, ?, ?, ?)";

        int affectedRows = executeModification(sql,
                link.getSupplierId(),
                link.getMedicineId(),
                link.getSupplyPrice(),
                link.getLeadTime()
        );

        return affectedRows > 0 ? link : null;
    }

    @Override
    public List<MedicineSupplier> getSuppliersByMedicineId(int medicineId) {
        String sql = SELECT_COLUMNS + "FROM medicine_supplier WHERE medicine_id = ?";
        return fetchMedicineSuppliers(sql, medicineId);
    }

    @Override
    public List<MedicineSupplier> getMedicinesBySupplierId(int supplierId) {
        String sql = SELECT_COLUMNS + "FROM medicine_supplier WHERE supplier_id = ?";
        return fetchMedicineSuppliers(sql, supplierId);
    }

    @Override
    public boolean deleteLink(int supplierId, int medicineId) {
        String sql = "DELETE FROM medicine_supplier WHERE supplier_id = ? AND medicine_id = ?";
        return executeModification(sql, supplierId, medicineId) > 0;
    }


    private int executeModification(String sql, Object... params) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Database Modification Failed] " + e.getMessage());
        }
        return 0;
    }

    private List<MedicineSupplier> fetchMedicineSuppliers(String sql, Object... params) {
        List<MedicineSupplier> links = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        links.add(new MedicineSupplier(
                                rs.getInt("supplier_id"),
                                rs.getInt("medicine_id"),
                                rs.getBigDecimal("supply_price"),
                                rs.getInt("lead_time")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch medicine-supplier links: " + e.getMessage());
        }
        return links;
    }
}