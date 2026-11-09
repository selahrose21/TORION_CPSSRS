package com.torion.dao;

import com.torion.model.ParkingSlot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSlotDao {

    public static List<ParkingSlot> getAllSlots() {
        List<ParkingSlot> slots = new ArrayList<>();
        String sql = "SELECT slot_id, slot_code, floor_level, slot_type, status FROM ParkingSlot ORDER BY slot_code";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                slots.add(new ParkingSlot(
                        rs.getInt("slot_id"),
                        rs.getString("slot_code"),
                        rs.getInt("floor_level"),
                        rs.getString("slot_type"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching parking slots: " + e.getMessage());
        }
        return slots;
    }

    public static boolean addSlot(int adminId, String slotCode, int floorLevel, String slotType) {
        String sql = "INSERT INTO ParkingSlot (slot_code, floor_level, slot_type, status) VALUES (?, ?, ?, 'VACANT')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, slotCode.toUpperCase());
            pstmt.setInt(2, floorLevel);
            pstmt.setString(3, slotType.toUpperCase());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ActivityLogDao.logAction(adminId, "ADD_SLOT", "Admin ID " + adminId + " added slot " + slotCode);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding parking slot: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateSlotStatus(int adminId, int slotId, String newStatus) {
        String sql = "UPDATE ParkingSlot SET status = ? WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.toUpperCase());
            pstmt.setInt(2, slotId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ActivityLogDao.logAction(adminId, "UPDATE_SLOT", "Admin ID " + adminId + " updated Slot ID " + slotId + " to " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating slot status: " + e.getMessage());
        }
        return false;
    }
}