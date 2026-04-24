package com.meditrack.model;

import com.meditrack.util.ValidationUtil;

import java.time.LocalDateTime;

public class AuditLog {
    private final int logId;
    private final int userId;
    private final String action;
    private final String tableName;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime timestamp;

    // Constructor A: For Database Retrieval
    public AuditLog(int logId, int userId, String action, String tableName,
                    String oldValue, String newValue, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = ValidationUtil.requireNonBlank(action, "Action");
        this.tableName = ValidationUtil.requireNonBlank(tableName, "Table Name");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }

    // Constructor B: For New Log Entry
    public AuditLog(int userId, String action, String tableName, String oldValue, String newValue) {
        this(0, userId, action, tableName, oldValue, newValue, LocalDateTime.now());
    }

    // Getters
    public int getLogId() { return logId; }
    public int getUserId() { return userId; }
    public String getAction() { return action; }
    public String getTableName() { return tableName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public LocalDateTime getTimestamp() { return timestamp; }
}