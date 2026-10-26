package com.torion.ui;

import com.torion.dao.UserDao;
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
            System.out.println("7. Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "5" -> updateUser(admin);
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

    private void approveUser(){
        List<User> pendingUsers = UserDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if(pendingUsers.isEmpty()){
            return;
        }

        System.out.print("Enter User ID to approver (or 0 to cancel): ");
        int userId = scanner.nextInt();

        if(userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
        }

        boolean exists = pendingUsers.stream().anyMatch(u ->u.user_id() == userId);
        if(!exists){
            System.out.println(" -> [ERROR] User ID not found.");
            return;
        }

        UserDao.approveUser(userId);

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

        userDao.updateUserAndStatus(userId, newRole, newStatus);
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
}
