package com.meditrack.dao;

import com.meditrack.model.AuditLog;
import com.meditrack.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO{
    private static final String SELECT_COLUMNS = "SELECT log_id, user_id, action, table_name, old_value, new_value, timestamp ";

    @Override
    public void logAction(AuditLog log) {
        String sql = "INSERT INTO audit_log (user_id, action, table_name, old_value, new_value) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, log.getUserId());
                stmt.setString(2, log.getAction());
                stmt.setString(3, log.getTableName());
                stmt.setString(4, log.getOldValue());
                stmt.setString(5, log.getNewValue());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Audit Log Failed] " + e.getMessage());
        }
    }


    @Override
    public List<AuditLog> getAllLogs() {
        return getPaginatedLogs(1);
    }

    @Override
    public List<AuditLog> getPaginatedLogs(int pageNumber) {
        if (pageNumber < 1) pageNumber = 1;

        int limit = 500;
        int offset = (pageNumber - 1) * limit;

        String sql = SELECT_COLUMNS + "FROM audit_log ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        return fetchLogs(sql, limit, offset);
    }

    @Override
    public List<AuditLog> getLogsByUser(int userId) {
        String sql = SELECT_COLUMNS + "FROM audit_log WHERE user_id = ? ORDER BY timestamp DESC LIMIT 500";
        return fetchLogs(sql, userId);
    }

    @Override
    public List<AuditLog> getLogsByTable(String tableName) {
        String sql = SELECT_COLUMNS + "FROM audit_log WHERE table_name = ? ORDER BY timestamp DESC LIMIT 500";
        return fetchLogs(sql, tableName);
    }

    @Override
    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = SELECT_COLUMNS + "FROM audit_log WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return fetchLogs(sql, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }


    private List<AuditLog> fetchLogs(String sql, Object... params) {
        List<AuditLog> logs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapRowToAuditLog(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Could not fetch logs: " + e.getMessage());
        }
        return logs;
    }

    private AuditLog mapRowToAuditLog(ResultSet rs) throws SQLException {
        return new AuditLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action"),
                rs.getString("table_name"),
                rs.getString("old_value"),
                rs.getString("new_value"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
