package com.meditrack.dao;

import com.meditrack.model.Address;
import com.meditrack.model.Supplier;
import com.meditrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAOImpl implements SupplierDAO {

    private static final String SELECT_COLUMNS = "SELECT supplier_id, name, contact, address_line1, address_line2, city, email ";

    @Override
    public Supplier saveSupplier(Supplier supplier) {
        String sql = "INSERT INTO supplier (name, contact, address_line1, address_line2, city, email) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, supplier.getName());
                stmt.setString(2, supplier.getContact());
                stmt.setString(3, supplier.getAddress().getAddressLine1());
                stmt.setString(4, supplier.getAddress().getAddressLine2());
                stmt.setString(5, supplier.getAddress().getCity());
                stmt.setString(6, supplier.getEmail());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int generatedId = rs.getInt(1);
                            return getSupplierById(generatedId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Supplier Save Failed] " + e.getMessage());
        }
        return null;
    }

    @Override
    public Supplier updateSupplier(Supplier supplier) {
        String sql = "UPDATE supplier SET name = ?, contact = ?, address_line1 = ?, address_line2 = ?, city = ?, email = ? WHERE supplier_id = ?";

        int affectedRows = executeModification(sql,
                supplier.getName(),
                supplier.getContact(),
                supplier.getAddress().getAddressLine1(),
                supplier.getAddress().getAddressLine2(),
                supplier.getAddress().getCity(),
                supplier.getEmail(),
                supplier.getSupplierId()
        );

        return affectedRows > 0 ? getSupplierById(supplier.getSupplierId()) : null;
    }

    @Override
    public boolean deleteSupplier(int id) {
        String sql = "DELETE FROM supplier WHERE supplier_id = ?";
        return executeModification(sql, id) > 0;
    }

    @Override
    public Supplier getSupplierById(int id) {
        String sql = SELECT_COLUMNS + "FROM supplier WHERE supplier_id = ?";
        List<Supplier> results = fetchSuppliers(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        String sql = SELECT_COLUMNS + "FROM supplier ORDER BY name ASC";
        return fetchSuppliers(sql);
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

    private List<Supplier> fetchSuppliers(String sql, Object... params) {
        List<Supplier> suppliers = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        suppliers.add(mapRowToSupplier(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch suppliers: " + e.getMessage());
        }
        return suppliers;
    }


    private Supplier mapRowToSupplier(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("address_line1"),
                rs.getString("address_line2"),
                rs.getString("city")
        );

        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("contact"),
                rs.getString("email"),
                address
        );
    }
}