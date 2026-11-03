package com.torion.dao;

import com.torion.model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public static void logAction(int userId, String actionTaken, String details) {
        String sql = "INSERT INTO SystemAuditLog (user_id, action_taken, details) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionTaken);
            pstmt.setString(3, details);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error writing to audit log: " + e.getMessage());
        }
    }

    public static List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT log_id, user_id, action_taken, details, timestamp FROM SystemAuditLog ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("action_taken"),
                        rs.getString("details"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs: " + e.getMessage());
        }
        return logs;
    }

    public static List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT log_id, user_id, action_taken, details, timestamp FROM SystemAuditLog WHERE user_id = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            rs.getString("action_taken"),
                            rs.getString("details"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for user: " + e.getMessage());
        }
        return logs;
    }
}