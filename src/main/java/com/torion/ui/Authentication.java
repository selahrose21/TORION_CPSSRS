package com.torion.ui;

import com.torion.dao.UserDao;
import com.torion.model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== WELCOME TO PARKSPOT ===");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[0] Exit");

            int choice = promptIntOrCancel(scanner, "Choose an option");

            if (choice == -1) {
                System.out.println("Exiting ParkSpot. Goodbye!");
                break;
            } else if (choice == 1) {
                handleLogin();
            } else if (choice == 2) {
                handleRegister();
            } else {
                System.out.println("Invalid option. Please choose 1, 2, or 0.");
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n=== LOGIN (Type 0 to Cancel) ===");
        String username = promptInputOrCancel(scanner, "Enter Username");
        if (username == null) {
            System.out.println("Login canceled.");
            return;
        }

        String password = promptInputOrCancel(scanner, "Enter Password");
        if (password == null) {
            System.out.println("Login canceled.");
            return;
        }

        User user = UserDao.login(username, password);
        if (user == null) {
            System.out.println("Invalid credentials or account is still PENDING approval.");
            return;
        }

        System.out.println("Welcome, " + user.full_name() + " (" + user.role() + ")!");
        switch (user.role().toUpperCase()) {
            case "ADMIN" -> new AdminMenu(userDao, scanner).display(user);
            case "ATTENDANT" -> new AttendantMenu(scanner).display(user);
            case "CUSTOMER" -> new CustomerMenu(scanner).display(user);
            default -> System.out.println("Unknown role.");
        }
    }

    private void handleRegister() {
        System.out.println("\n=== REGISTER NEW ACCOUNT (Type 0 to Cancel) ===");

        // 1. Full Name with Cancel Option
        String fullName = promptInputOrCancel(scanner, "Enter Full Name");
        if (fullName == null) {
            System.out.println("Registration canceled. Returning to main menu...");
            return;
        }

        // 2. Username with Cancel Option
        String username = promptInputOrCancel(scanner, "Enter Username");
        if (username == null) {
            System.out.println("Registration canceled. Returning to main menu...");
            return;
        }

        // 3. Password with Cancel Option
        String password = promptInputOrCancel(scanner, "Enter Password");
        if (password == null) {
            System.out.println("Registration canceled. Returning to main menu...");
            return;
        }

        // 4. Role Selection Loop (Retries on invalid input + 0 to Cancel)
        String role = null;
        while (role == null) {
            System.out.println("\nSelect Role:");
            System.out.println("[1] Customer");
            System.out.println("[2] Attendant");
            System.out.println("[0] Cancel Registration");

            int roleChoice = promptIntOrCancel(scanner, "Choose option");

            if (roleChoice == -1) {
                System.out.println("Registration canceled. Returning to main menu...");
                return;
            } else if (roleChoice == 1) {
                role = "CUSTOMER";
            } else if (roleChoice == 2) {
                role = "ATTENDANT";
            } else {
                System.out.println("Invalid choice! Please enter 1 for Customer, 2 for Attendant, or 0 to cancel.");
            }
        }

        // 5. Save to Database via UserDao
        boolean success = UserDao.registerUser(fullName, username, password, role);
        if (success) {
            System.out.println("Registration successful! Your account status is PENDING admin approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private static String promptInputOrCancel(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " (or 0 to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private static int promptIntOrCancel(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " (or 0 to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                return -1;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter digits only.");
            }
        }
    }
}