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

        String createParkingSlotTable = """
            CREATE TABLE IF NOT EXISTS ParkingSlot (
                slot_id INTEGER PRIMARY KEY AUTOINCREMENT,
                slot_code TEXT UNIQUE NOT NULL,
                floor_level INTEGER NOT NULL,
                slot_type TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'VACANT'
            );
            """;

        // 1. Define your ParkingTicket table creation string here
        String createParkingTicketTable = """
            CREATE TABLE IF NOT EXISTS ParkingTicket (
                ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
                slot_id INTEGER NOT NULL,
                plate_number TEXT NOT NULL,
                entry_time DATETIME DEFAULT (datetime('now', '+8 hours')),
                exit_time DATETIME,
                total_fee REAL DEFAULT 0.0,
                payment_status TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY (slot_id) REFERENCES ParkingSlot(slot_id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 2. Execute all table creation scripts here
            stmt.execute(createActivityLogsTable);
            stmt.execute(createParkingSlotTable);
            stmt.execute(createParkingTicketTable); // <-- This is where you call it!

            // Seed default slots if table is empty
            seedDefaultSlots(stmt);

        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
        }

        new UserDao();
        System.out.println(" -> Database tables verified");
    }

    private static void seedDefaultSlots(Statement stmt) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM ParkingSlot";
        try (var rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String seedSql = """
                    INSERT INTO ParkingSlot (slot_code, floor_level, slot_type, status) VALUES 
                    ('A1', 1, 'Car', 'VACANT'),
                    ('A2', 1, 'Car', 'VACANT'),
                    ('A3', 1, 'Car', 'OCCUPIED'),
                    ('A4', 1, 'Motorcycle', 'VACANT'),
                    ('A5', 1, 'Motorcycle', 'OUT_OF_ORDER');
                """;
                stmt.executeUpdate(seedSql);
                System.out.println("-> Default Parking Slots (A1-A5) seeded.");
            }
        }
    }
}