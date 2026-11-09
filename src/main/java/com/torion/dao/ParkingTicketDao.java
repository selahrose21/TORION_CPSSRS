package com.torion.dao;

import com.torion.model.ParkingTicket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingTicketDao {

    // Feature 1: Create new parking ticket upon vehicle arrival
    public static boolean checkInVehicle(int userId, String plateNumber, int slotId) {
        String checkSlotSql = "SELECT status FROM ParkingSlot WHERE slot_id = ?";
        String insertTicketSql = "INSERT INTO ParkingTicket (slot_id, plate_number, payment_status) VALUES (?, ?, 'PENDING')";
        String updateSlotSql = "UPDATE ParkingSlot SET status = 'OCCUPIED' WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction block

            // 1. Verify slot is vacant
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSlotSql)) {
                pstmtCheck.setInt(1, slotId);
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (!rs.next() || !"VACANT".equalsIgnoreCase(rs.getString("status"))) {
                        System.out.println(" -> [ERROR] Slot is not available or does not exist.");
                        return false;
                    }
                }
            }

            // 2. Insert parking ticket
            try (PreparedStatement pstmtTicket = conn.prepareStatement(insertTicketSql)) {
                pstmtTicket.setInt(1, slotId);
                pstmtTicket.setString(2, plateNumber.toUpperCase());
                pstmtTicket.executeUpdate();
            }

            // 3. Update slot status to OCCUPIED
            try (PreparedStatement pstmtSlot = conn.prepareStatement(updateSlotSql)) {
                pstmtSlot.setInt(1, slotId);
                pstmtSlot.executeUpdate();
            }

            conn.commit();
            ActivityLogDao.logAction(userId, "VEHICLE_CHECK_IN", "Vehicle " + plateNumber + " checked into Slot ID " + slotId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error during vehicle check-in: " + e.getMessage());
            return false;
        }
    }

    // Feature 2: View active parking tickets
    public static List<ParkingTicket> getActiveTickets() {
        List<ParkingTicket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM ParkingTicket WHERE payment_status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tickets.add(new ParkingTicket(
                        rs.getInt("ticket_id"),
                        rs.getInt("slot_id"),
                        rs.getString("plate_number"),
                        rs.getString("entry_time"),
                        rs.getString("exit_time"),
                        rs.getDouble("total_fee"),
                        rs.getString("payment_status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active tickets: " + e.getMessage());
        }
        return tickets;
    }

    // Feature 3: Process exit checkout and calculate fee (e.g., ₱30/hour base rate)
    public static boolean checkOutVehicle(int userId, int ticketId, int slotId) {
        String updateTicketSql = """
            UPDATE ParkingTicket 
            SET exit_time = datetime('now', '+8 hours'), 
                total_fee = (strftime('%s', datetime('now', '+8 hours')) - strftime('%s', entry_time)) / 3600.0 * 30.0, 
                payment_status = 'PAID' 
            WHERE ticket_id = ?
            """;
        String updateSlotSql = "UPDATE ParkingSlot SET status = 'VACANT' WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtTicket = conn.prepareStatement(updateTicketSql)) {
                pstmtTicket.setInt(1, ticketId);
                pstmtTicket.executeUpdate();
            }

            try (PreparedStatement pstmtSlot = conn.prepareStatement(updateSlotSql)) {
                pstmtSlot.setInt(1, slotId);
                pstmtSlot.executeUpdate();
            }

            conn.commit();
            ActivityLogDao.logAction(userId, "VEHICLE_CHECK_OUT", "Processed checkout for Ticket ID " + ticketId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error during vehicle check-out: " + e.getMessage());
            return false;
        }
    }
}