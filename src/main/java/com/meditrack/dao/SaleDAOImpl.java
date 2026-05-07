package com.meditrack.dao;

import com.meditrack.model.Sale;
import com.meditrack.model.SaleItem;
import com.meditrack.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAOImpl implements SaleDAO {

    @Override
    public Sale processSale(Sale sale) {
        String insertSaleSql = "INSERT INTO sale (user_id, item_count, total) VALUES (?, ?, ?)";
        String insertItemSql = "INSERT INTO sale_item (sale_id, medicine_id, quantity, unit_price_at_sale) VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Save the main receipt to the sale table
            int generatedSaleId = 0;
            try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                saleStmt.setInt(1, sale.getUserId());
                saleStmt.setInt(2, sale.getItems().size());
                saleStmt.setBigDecimal(3, sale.getTotal());
                saleStmt.executeUpdate();

                try (ResultSet rs = saleStmt.getGeneratedKeys()) {
                    if (rs.next()) generatedSaleId = rs.getInt(1);
                }
            }

            // Loop through the cart and save every item to sale_item table
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (SaleItem item : sale.getItems()) {
                    itemStmt.setInt(1, generatedSaleId);
                    itemStmt.setInt(2, item.getMedicineId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPriceAtSale());
                    itemStmt.executeUpdate();
                }
            }

            conn.commit();
            return getSaleById(generatedSaleId);

        } catch (SQLException e) {
            System.err.println("[Sale Processing Failed] " + e.getMessage());
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
    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sale WHERE sale_id = ?";
        List<Sale> results = fetchSales(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Sale> getAllSales() {
        String sql = "SELECT * FROM sale ORDER BY purchase_date DESC";
        return fetchSales(sql);
    }

    @Override
    public List<Sale> getAllSalesByUserId(int userId) {
        String sql = "SELECT * FROM sale WHERE user_id = ? ORDER BY purchase_date DESC";
        return fetchSales(sql, userId);
    }

    @Override
    public List<Sale> getAllSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM sale WHERE purchase_date BETWEEN ? AND ? ORDER BY purchase_date DESC";

        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        return fetchSales(sql, startTimestamp, endTimestamp);
    }



    private List<Sale> fetchSales(String sql, Object... params) {
        List<Sale> sales = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int saleId = rs.getInt("sale_id");
                        List<SaleItem> items = fetchSaleItemsForSaleId(saleId, conn);
                        Timestamp dbTime = rs.getTimestamp("purchase_date");
                        LocalDateTime purchaseDate = (dbTime != null) ? dbTime.toLocalDateTime() : LocalDateTime.now();

                        sales.add(new Sale(
                                saleId,
                                rs.getInt("user_id"),
                                items,
                                rs.getBigDecimal("total"),
                                purchaseDate
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch sales: " + e.getMessage());
        }
        return sales;
    }


    private List<SaleItem> fetchSaleItemsForSaleId(int saleId, Connection conn) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_item WHERE sale_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new SaleItem(
                            rs.getInt("sale_item_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price_at_sale")
                    ));
                }
            }
        }
        return items;
    }
}