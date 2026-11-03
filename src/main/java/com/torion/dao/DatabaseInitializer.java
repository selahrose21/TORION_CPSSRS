package com.torion.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS SystemAuditLog (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_taken TEXT NOT NULL,
                timestamp DATETIME DEFAULT (datetime('now', '+8 hours')),
                details TEXT,
                FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createActivityLogsTable);

        } catch (SQLException e) {
            System.err.println("Error creating activity_logs table: " + e.getMessage());
        }

        new UserDao();
        System.out.println(" -> Database tables verified");
    }
}