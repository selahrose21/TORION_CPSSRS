package com.torion.ui;

import com.torion.dao.ActivityLogDao;
import com.torion.dao.ParkingSlotDao;
import com.torion.dao.UserDao;
import com.torion.model.ActivityLog;
import com.torion.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==================================================");
            System.out.println("                 ADMIN DASHBOARD                  ");
            System.out.println("==================================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Manage Parking Slots");
            System.out.println("9. Manage Parking Tickets");
            System.out.println("10. Generate Daily Reports (TXT/CSV)");
            System.out.println("11. Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choose an option (1-11): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "4" -> {
                    String fullName = promptInputOrCancel(scanner, "Enter Full Name");
                    if (fullName == null) {
                        System.out.println(" -> Operation Cancelled.");
                        break;
                    }

                    String newUsername = promptInputOrCancel(scanner, "Enter new username");
                    if (newUsername == null) {
                        System.out.println(" -> Operation Cancelled.");
                        break;
                    }

                    String newPassword = promptInputOrCancel(scanner, "Enter password");
                    if (newPassword == null) {
                        System.out.println(" -> Operation Cancelled.");
                        break;
                    }

                    String role = readValidRole();
                    if (role == null) {
                        System.out.println(" -> Operation Cancelled.");
                        break;
                    }

                    boolean success = userDao.addUser(admin.user_id(), fullName, newUsername, newPassword, role, "ACTIVE");
                    if (success) {
                        System.out.println("[SUCCESS] User added successfully with role " + role + "!");
                    } else {
                        System.out.println("[ERROR] Failed to add user.");
                    }
                }
                case "5" -> updateUser(admin);
                case "6" -> {
                    System.out.println("\n--- Current Users List ---");
                    viewAllUser();

                    int targetId = promptIntOrCancel(scanner, "Enter target User ID to delete");
                    if (targetId == -1) {
                        System.out.println(" -> Operation Cancelled.");
                        break;
                    }

                    boolean success = userDao.deleteUser(admin.user_id(), targetId);
                    if (success) {
                        System.out.println("[SUCCESS] User deleted and audit log recorded.");
                    } else {
                        System.out.println("[ERROR] Failed to delete user (ID may not exist).");
                    }
                }
                case "7" -> auditLogSubMenu();
                case "8" -> parkingSlotSubMenu(admin);
                case "9" -> parkingTicketSubMenu(admin);
                case "10" -> com.torion.dao.ReportDao.generateDailyReport(admin.user_id());
                case "11" -> {
                    ActivityLogDao.logAction(admin.user_id(), "USER_LOGOUT", "User " + admin.username() + " logged out.");
                    loggedIn = false;
                    System.out.println("-> Logged out successfully. Returning to main menu...");
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void displayUserTable(List<User> User, String title){
        System.out.println("\n===============================" +title+ "===============================");
        if(User.isEmpty()){
            System.out.println(" -> No Records Found.");
            return;
        }

        System.out.println("+-------+-------------------+-------------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-15s | %-10s | %-10s |\n", "ID", "Full name", "Username", "Role", "Status");
        System.out.println("+-------+-------------------+-------------------+------------+------------+");
        for(User u : User){
            System.out.printf("| %-3s | %-15s | %-15s | %-10s | %-10s |\n",
                    u.user_id(), u.full_name(), u.username(), u.role(), u.status());
        }
        System.out.println("+-------+-------------------+-------------------+------------+------------+");
    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");
    }

    private void viewPendingUsers(){
        displayUserTable(UserDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void approveUser(User admin) {
        List<User> pendingUsers = UserDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if(pendingUsers.isEmpty()){
            return;
        }

        int userId = promptIntOrCancel(scanner, "Enter User ID to approve");
        if(userId == -1){
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.user_id() == userId);
        if(!exists){
            System.out.println(" -> [ERROR] User ID not found.");
            return;
        }

        UserDao.approveUser(userId, admin.user_id());
    }

    private void updateUser(User currentAdmin){
        List<User> User = UserDao.getAllUser();
        displayUserTable(User, "SELECT USER TO UPDATE");

        if (User.isEmpty()){
            return;
        }

        int userId = promptIntOrCancel(scanner, "Enter selected ID to update");
        if(userId == -1){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if(userId == currentAdmin.user_id()){
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
            return;
        }

        String newRole = readValidRole();
        if(newRole == null){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        String newStatus = readValidStatus();
        if(newStatus == null){
            System.out.println(" -> Operation Cancelled");
            return;
        }

        userDao.updateUserAndStatus(userId, newRole, newStatus, currentAdmin.user_id());
    }

    private String readValidRole(){
        while(true){
            System.out.print("Enter Role (ADMIN/ATTENDANT/CUSTOMER) [press 0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if(role.equals("0") || role.equals("CANCEL")){
                return null;
            }

            if(role.equals("ADMIN") || role.equals("ATTENDANT") || role.equals("CUSTOMER")){
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }
    }

    private String readValidStatus(){
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [press 0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if(status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println(" -> [ERROR] Invalid status. Must be APPROVED or PENDING.");
        }
    }

    private void auditLogSubMenu() {
        System.out.println("\n--- AUDIT LOG MENU ---");
        System.out.println("1. View All System Logs");
        System.out.println("2. Filter Logs by User ID");
        System.out.print("Choose an option (or press 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (choice.equals("1")) {
            displayLogTable(ActivityLogDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
        } else if (choice.equals("2")) {
            int targetUserId = promptIntOrCancel(scanner, "Enter User ID to filter logs");
            if (targetUserId == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }
            displayLogTable(ActivityLogDao.getLogsByUserId(targetUserId), "LOGS FOR USER ID " + targetUserId);
        } else {
            System.out.println(" -> [ERROR] Invalid Selection.");
        }
    }

    private void displayLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n-------------------------------------------------- " + title + " --------------------------------------------------");
        if (logs.isEmpty()) {
            System.out.println(" -> No Logs Found.");
            return;
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-8s | %-8s | %-20s | %-20s | %-45s |\n", "Log ID", "User ID", "Action Taken", "Timestamp", "Details");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        for (ActivityLog log : logs) {
            System.out.printf("| %-8d | %-8d | %-20s | %-20s | %-45s |\n",
                    log.log_id(),
                    log.userId(),
                    log.action_taken(),
                    log.timestamp(),
                    log.details()
            );
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
    }

    private void parkingSlotSubMenu(User admin) {
        System.out.println("\n--- PARKING SLOT MANAGEMENT ---");
        System.out.println("1. View Parking Slot Map");
        System.out.println("2. Add New Parking Slot");
        System.out.println("3. Update Slot Status");
        System.out.print("Choose an option (or press 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (choice.equals("1")) {
            displaySlotMap(ParkingSlotDao.getAllSlots());
        } else if (choice.equals("2")) {
            String code = promptInputOrCancel(scanner, "Enter Slot Code (e.g., A6)");
            if (code == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            int floor = promptIntOrCancel(scanner, "Enter Floor Level");
            if (floor == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            String type = promptInputOrCancel(scanner, "Enter Slot Type (Car / Motorcycle)");
            if (type == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            if (ParkingSlotDao.addSlot(admin.user_id(), code, floor, type)) {
                System.out.println("[SUCCESS] Parking slot added successfully!");
            } else {
                System.out.println("[ERROR] Failed to add slot (Code might already exist).");
            }
        } else if (choice.equals("3")) {
            displaySlotMap(ParkingSlotDao.getAllSlots());
            int slotId = promptIntOrCancel(scanner, "Enter Slot ID to update");
            if (slotId == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            String status = promptInputOrCancel(scanner, "Enter New Status (VACANT / OCCUPIED / OUT_OF_ORDER)");
            if (status == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            if (ParkingSlotDao.updateSlotStatus(admin.user_id(), slotId, status)) {
                System.out.println("[SUCCESS] Slot status updated!");
            } else {
                System.out.println("[ERROR] Failed to update slot status.");
            }
        } else {
            System.out.println(" -> [ERROR] Invalid Selection.");
        }
    }

    private void displaySlotMap(List<com.torion.model.ParkingSlot> slots) {
        System.out.println("\n============================= PARKING SLOT MAP =============================");
        if (slots.isEmpty()) {
            System.out.println(" -> No Parking Slots Found.");
            return;
        }

        System.out.println("+---------+-----------+-------------+---------------+------------------+");
        System.out.printf("| %-7s | %-9s | %-11s | %-13s | %-16s |\n", "Slot ID", "Slot Code", "Floor Level", "Vehicle Type", "Status");
        System.out.println("+---------+-----------+-------------+---------------+------------------+");
        for (com.torion.model.ParkingSlot s : slots) {
            System.out.printf("| %-7d | %-9s | %-11d | %-13s | %-16s |\n",
                    s.slot_id(), s.slot_code(), s.floor_level(), s.slot_type(), s.status());
        }
        System.out.println("+---------+-----------+-------------+---------------+------------------+");
    }

    private void parkingTicketSubMenu(User admin) {
        System.out.println("\n--- PARKING TICKET MANAGEMENT ---");
        System.out.println("1. Check-In Vehicle (Create Ticket)");
        System.out.println("2. View Active Parking Tickets");
        System.out.println("3. Check-Out Vehicle (Process Payment)");
        System.out.print("Choose an option (or press 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (choice.equals("1")) {
            displaySlotMap(com.torion.dao.ParkingSlotDao.getAllSlots());

            String plateNumber = promptInputOrCancel(scanner, "Enter Plate Number");
            if (plateNumber == null) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            int slotId = promptIntOrCancel(scanner, "Enter Target Slot ID to occupy");
            if (slotId == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            boolean success = com.torion.dao.ParkingTicketDao.checkInVehicle(admin.user_id(), plateNumber, slotId);
            if (success) {
                System.out.println("[SUCCESS] Vehicle checked in successfully and slot marked OCCUPIED!");
            } else {
                System.out.println("[ERROR] Failed to check in vehicle. Check slot availability.");
            }
        } else if (choice.equals("2")) {
            displayActiveTicketTable(com.torion.dao.ParkingTicketDao.getActiveTickets(), "ACTIVE PARKING TICKETS");
        } else if (choice.equals("3")) {
            List<com.torion.model.ParkingTicket> activeTickets = com.torion.dao.ParkingTicketDao.getActiveTickets();
            displayActiveTicketTable(activeTickets, "ACTIVE TICKETS FOR CHECKOUT");

            if (activeTickets.isEmpty()) return;

            int ticketId = promptIntOrCancel(scanner, "Enter Ticket ID to check out");
            if (ticketId == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            int slotId = promptIntOrCancel(scanner, "Enter corresponding Slot ID being vacated");
            if (slotId == -1) {
                System.out.println(" -> Operation Cancelled.");
                return;
            }

            boolean success = com.torion.dao.ParkingTicketDao.checkOutVehicle(admin.user_id(), ticketId, slotId);
            if (success) {
                System.out.println("[SUCCESS] Vehicle checked out successfully, fee calculated, and slot is now VACANT!");
            } else {
                System.out.println("[ERROR] Failed to process checkout.");
            }
        } else {
            System.out.println(" -> [ERROR] Invalid Selection.");
        }
    }

    private void displayActiveTicketTable(List<com.torion.model.ParkingTicket> tickets, String title) {
        System.out.println("\n============================= " + title + " =============================");
        if (tickets.isEmpty()) {
            System.out.println(" -> No Active Tickets Found.");
            return;
        }

        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
        System.out.printf("| %-9s | %-7s | %-12s | %-19s | %-14s |\n", "Ticket ID", "Slot ID", "Plate Number", "Entry Time", "Status");
        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
        for (com.torion.model.ParkingTicket t : tickets) {
            System.out.printf("| %-9d | %-7d | %-12s | %-19s | %-14s |\n",
                    t.ticket_id(), t.slot_id(), t.plate_number(), t.entry_time(), t.payment_status());
        }
        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
    }

    // --- Helper Methods for Input & Cancellation ---

    private String promptInputOrCancel(Scanner scanner, String promptMessage) {
        while (true) {
            System.out.print(promptMessage + " (or press 0 to cancel): ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null; // Signals cancellation
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(" -> [ERROR] Input cannot be empty. Try again.");
        }
    }

    private int promptIntOrCancel(Scanner scanner, String promptMessage) {
        while (true) {
            System.out.print(promptMessage + " (or press 0 to cancel): ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return -1; // -1 represents cancellation for numeric IDs
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid number.");
            }
        }
    }
}