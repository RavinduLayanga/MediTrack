package com.meditrack.dao;

import com.meditrack.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogDAO {
    void logAction(AuditLog log);
    List<AuditLog> getAllLogs();
    List<AuditLog> getLogsByUser(int userId);
    List<AuditLog> getLogsByTable(String tableName);
    List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end);
}
