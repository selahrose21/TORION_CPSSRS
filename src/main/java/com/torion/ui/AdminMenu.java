package com.torion.ui;

import com.torion.dao.ActivityLogDao;
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
            System.out.println("8. Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "5" -> updateUser(admin);
                case "7" -> auditLogSubMenu();
                case "8" -> {
                    ActivityLogDao.logAction(admin.user_id(), "USER_LOGOUT", "User " + admin.username() + " logged out.");
                    loggedIn = false;
                    System.out.println(" -> Logged out successfully. Returning to main menu...");
                }
                default -> System.out.println(" -> [ERROR] Invalid Option.");
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

        int userId = readIntInput("Enter User ID to approve (or 0 to cancel): ");

        if(userId == 0){
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

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if(userId == 0){
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
            System.out.print("Enter Role (ADMIN/ATTENDANT/CUSTOMER (or 0 to cancel):");
            String role = scanner.nextLine().trim().toUpperCase();
            if(role.equals("0") || role.equals("CANCEL")){
                return null;
            }

            if(role.equals("ADMIN") || role.equals("ATTENDANT") || role.equals("CUSTOMER")){
                return role;
            }
            System.out.println(" ->[ERROR] Invalid Role.");
        }
    }

    private String readValidStatus(){
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
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

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    private void auditLogSubMenu() {
        System.out.println("\n--- AUDIT LOG MENU ---");
        System.out.println("1. View All System Logs");
        System.out.println("2. Filter Logs by User ID");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            displayLogTable(ActivityLogDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
        } else if (choice.equals("2")) {
            int targetUserId = readIntInput("Enter User ID to filter logs: ");
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
}
