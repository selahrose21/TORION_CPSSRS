package com.torion.dao;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ReportDao {

    public static void generateDailyReport(int adminId) {
        String query = """
            SELECT 
                COUNT(*) AS total_vehicles,
                COALESCE(SUM(total_fee), 0.0) AS total_revenue,
                COALESCE(AVG((strftime('%s', exit_time) - strftime('%s', entry_time)) / 60.0), 0.0) AS avg_duration_mins
            FROM ParkingTicket
            WHERE payment_status = 'PAID'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int totalVehicles = rs.getInt("total_vehicles");
                double totalRevenue = rs.getDouble("total_revenue");
                double avgDuration = rs.getDouble("avg_duration_mins");

                // 1. On-Screen CLI Display
                System.out.println("\n==================================================");
                System.out.println("   DAILY PARKING COLLECTIONS & UTILIZATION REPORT");
                System.out.println("==================================================");
                System.out.printf(" Total Vehicles Served  : %d\n", totalVehicles);
                System.out.printf(" Total Daily Revenue    : ₱%.2f\n", totalRevenue);
                System.out.printf(" Avg. Parking Duration  : %.2f minutes\n", avgDuration);
                System.out.println("--------------------------------------------------");

                // 2. Export to TXT File
                String txtFilename = "Daily_Parking_Report.txt";
                try (FileWriter writer = new FileWriter(txtFilename)) {
                    writer.write("PARKSPOT - DAILY PARKING COLLECTIONS & UTILIZATION REPORT\n");
                    writer.write("--------------------------------------------------------\n");
                    writer.write("Total Vehicles Served: " + totalVehicles + "\n");
                    writer.write("Total Daily Revenue: P" + String.format("%.2f", totalRevenue) + "\n");
                    writer.write("Avg. Parking Duration: " + String.format("%.2f", avgDuration) + " minutes\n");
                    System.out.println(" [SUCCESS] Exported report to " + txtFilename);
                }

                // 3. Export to CSV File
                String csvFilename = "Daily_Parking_Report.csv";
                try (FileWriter writer = new FileWriter(csvFilename)) {
                    writer.write("Metric,Value\n");
                    writer.write("Total Vehicles Served," + totalVehicles + "\n");
                    writer.write("Total Daily Revenue," + totalRevenue + "\n");
                    writer.write("Avg Parking Duration (mins)," + avgDuration + "\n");
                    System.out.println(" [SUCCESS] Exported report to " + csvFilename);
                }

                // ActivityLogDao.logAction(adminId, "GENERATE_REPORT", "Admin generated daily parking collections and utilization report.");

            }
        } catch (SQLException | IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }
}