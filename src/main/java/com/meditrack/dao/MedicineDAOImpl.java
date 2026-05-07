package com.meditrack.dao;

import com.meditrack.model.Category;
import com.meditrack.model.Liquid;
import com.meditrack.model.Medicine;
import com.meditrack.model.Tablet;
import com.meditrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAOImpl implements MedicineDAO {


    @Override
    public Medicine saveMedicine(Medicine medicine) {
        String baseSql = "INSERT INTO medicine (name, brand, type, category_id, stock, price) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Save to the medicine table
            try (PreparedStatement stmt = conn.prepareStatement(baseSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, medicine.getName());
                stmt.setString(2, medicine.getBrand());

                String type = (medicine instanceof Tablet) ? "TABLET" : "LIQUID";
                stmt.setString(3, type);
                stmt.setInt(4, medicine.getCategory().getCategoryId());
                stmt.setInt(5, medicine.getStock());
                stmt.setBigDecimal(6, medicine.getPrice());

                stmt.executeUpdate();

                // Get the generated ID
                int generatedId = 0;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) generatedId = rs.getInt(1);
                }

                // Save to the subclass tables
                if (medicine instanceof Tablet) {
                    String tabletSql = "INSERT INTO tablets (medicine_id, dosage) VALUES (?, ?)";
                    try (PreparedStatement tStmt = conn.prepareStatement(tabletSql)) {
                        tStmt.setInt(1, generatedId);
                        tStmt.setString(2, ((Tablet) medicine).getDosage());
                        tStmt.executeUpdate();
                    }
                } else if (medicine instanceof Liquid) {
                    String liquidSql = "INSERT INTO liquids (medicine_id, volume) VALUES (?, ?)";
                    try (PreparedStatement lStmt = conn.prepareStatement(liquidSql)) {
                        lStmt.setInt(1, generatedId);
                        lStmt.setString(2, ((Liquid) medicine).getVolume());
                        lStmt.executeUpdate();
                    }
                }

                conn.commit();
                return getMedicineById(generatedId);
            }
        } catch (SQLException e) {
            System.err.println("[Medicine Save Failed] " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    @Override
    public Medicine getMedicineById(int id) {
        String sql = "SELECT m.*, c.category_name, t.dosage, l.volume " +
                "FROM medicine m " +
                "JOIN category c ON m.category_id = c.category_id " +
                "LEFT JOIN tablets t ON m.medicine_id = t.medicine_id " +
                "LEFT JOIN liquids l ON m.medicine_id = l.medicine_id " +
                "WHERE m.medicine_id = ?";

        List<Medicine> results = fetchMedicines(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Medicine updateMedicine(Medicine medicine) {
        String baseSql = "UPDATE medicine SET name = ?, brand = ?, category_id = ?, stock = ?, price = ? WHERE medicine_id = ?";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Update the table
            try (PreparedStatement stmt = conn.prepareStatement(baseSql)) {
                stmt.setString(1, medicine.getName());
                stmt.setString(2, medicine.getBrand());
                stmt.setInt(3, medicine.getCategory().getCategoryId());
                stmt.setInt(4, medicine.getStock());
                stmt.setBigDecimal(5, medicine.getPrice());
                stmt.setInt(6, medicine.getMedicineId());
                stmt.executeUpdate();
            }

            // Update the subclass tables
            if (medicine instanceof Tablet) {
                String tabletSql = "UPDATE tablets SET dosage = ? WHERE medicine_id = ?";
                try (PreparedStatement tStmt = conn.prepareStatement(tabletSql)) {
                    tStmt.setString(1, ((Tablet) medicine).getDosage());
                    tStmt.setInt(2, medicine.getMedicineId());
                    tStmt.executeUpdate();
                }
            } else if (medicine instanceof Liquid) {
                String liquidSql = "UPDATE liquids SET volume = ? WHERE medicine_id = ?";
                try (PreparedStatement lStmt = conn.prepareStatement(liquidSql)) {
                    lStmt.setString(1, ((Liquid) medicine).getVolume());
                    lStmt.setInt(2, medicine.getMedicineId());
                    lStmt.executeUpdate();
                }
            }

            conn.commit();
            return getMedicineById(medicine.getMedicineId());

        } catch (SQLException e) {
            System.err.println("[Medicine Update Failed] " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    @Override
    public List<Medicine> getAllMedicines() {
        String sql = "SELECT m.*, c.category_name, t.dosage, l.volume " +
                "FROM medicine m " +
                "JOIN category c ON m.category_id = c.category_id " +
                "LEFT JOIN tablets t ON m.medicine_id = t.medicine_id " +
                "LEFT JOIN liquids l ON m.medicine_id = l.medicine_id";

        return fetchMedicines(sql);
    }

    @Override
    public boolean deleteMedicine(int id) {
        String sql = "DELETE FROM medicine WHERE medicine_id = ?";
        return executeModification(sql, id) > 0;

    }

    private int executeModification(String sql, Object...params) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[Database Modification Failed] " + e.getMessage());
        }
        return 0;
    }


    private List <Medicine> fetchMedicines(String sql, Object... params){
        List <Medicine> medicines = new ArrayList<>();

        try{
            Connection conn = DBConnection.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                for (int i = 0; i < params.length; i++){
                    stmt.setObject(i + 1, params[i]);
                }
                try(ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        medicines.add(mapRowToMedicine(rs));
                    }
                }
            }

        }catch(Exception e){
            System.err.println("[Database Error] Could not fetch medicines: " + e.getMessage());
        }
        return medicines;

    }

    private Medicine mapRowToMedicine(ResultSet rs) throws SQLException {
        Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
        String type = rs.getString("type");
        java.sql.Timestamp dbTime = rs.getTimestamp("updated_date");
        java.time.LocalDateTime updatedTime = (dbTime != null) ? dbTime.toLocalDateTime() : java.time.LocalDateTime.now();

        if ("TABLET".equals(type)) {
            return new Tablet(
                    rs.getInt("medicine_id"),
                    rs.getString("name"),
                    rs.getString("brand"),
                    category,
                    rs.getInt("stock"),
                    rs.getBigDecimal("price"),
                    rs.getString("dosage"),
                    updatedTime
            );
        } else {
            return new Liquid(
                    rs.getInt("medicine_id"),
                    rs.getString("name"),
                    rs.getString("brand"),
                    category,
                    rs.getInt("stock"),
                    rs.getBigDecimal("price"),
                    rs.getString("volume"),
                    updatedTime
            );
        }
    }
}
