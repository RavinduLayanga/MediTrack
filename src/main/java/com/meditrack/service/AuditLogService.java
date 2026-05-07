package com.meditrack.service;

import com.meditrack.dao.AuditLogDAO;
import com.meditrack.model.AuditLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogService {
    private final AuditLogDAO auditLogDAO;

    public AuditLogService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    // Create log entry
    public void recordAction(int userId, String action, String tableName, String oldValue, String newValue) {
        AuditLog log = new AuditLog(userId, action, tableName, oldValue, newValue);
        auditLogDAO.logAction(log);
    }

    // Retrieve logs with pagination
    public List<AuditLog> getSystemHistory(int page) {
        if (page < 1) {
            System.out.println("[Warning] Page number cannot be less than 1. Defaulting to Page 1.");
            page = 1;
        }
        return auditLogDAO.getPaginatedLogs(page);
    }

    public List<AuditLog> getLogsForUser(int userId) {
        return auditLogDAO.getLogsByUser(userId);
    }

    public List<AuditLog> getLogsByTableName(String tableName) {
        return auditLogDAO.getLogsByTable(tableName);
    }

    public List<AuditLog> getHistoryInDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            System.out.println("[Error] Search Failed: The start date must be before the end date.");
            return new ArrayList<>();
        }
        return auditLogDAO.getLogsByDateRange(start, end);
    }
}