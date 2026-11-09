package com.torion.ui;

import com.torion.dao.ActivityLogDao;
import com.torion.dao.UserDao;
import com.torion.model.User;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start(){
        boolean running = true;
        while (running) {
            System.out.println("\n======================================");
            System.out.println("\t   PARKSPOT SYSTEM MENU              ");
            System.out.println("======================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> {
                    System.out.print("Enter full name: ");
                    String fname = sc.nextLine().trim();
                    System.out.print("Enter username: ");
                    String user = sc.nextLine().trim();
                    System.out.print("Enter password: ");
                    String pass = sc.nextLine().trim();

                    System.out.print("Select Role [1] Admin [2] Attendant [3] Customer: ");
                    int roleChoice = sc.nextInt();
                    sc.nextLine(); // consume newline

                    String role;
                    switch (roleChoice) {
                        case 1 -> role = "ADMIN";
                        case 2 -> role = "ATTENDANT";
                        case 3 -> role = "CUSTOMER";
                        default -> role = null;
                    }

                    if (role != null) {
                        UserDao.registerUser(fname, user, pass, role);
                        System.out.println("[SUCCESS] Registration complete! You can now log in.");
                    } else {
                        System.out.println(" -> [ERROR] Invalid role selection!");
                    }
                } // Added missing break/block boundary
                case "3" -> {
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password_hash = sc.nextLine().trim();

        User user = userDao.login(username, password_hash);

        if (user != null){
            ActivityLogDao.logAction(user.user_id(), "USER_LOGIN", "User " + user.username() + " logged into the system.");

            System.out.println("\n -> Welcome " + user.username() + " | ROLE: " + user.role() + "");
            switch(user.role()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                case "ATTENDANT" -> new AttendantMenu(sc).display(user);
                case "CUSTOMER" -> new CustomerMenu(sc).display(user);
                default -> System.out.println(" -> [ERROR] Account role dashboard not found.");
            }
        } else {
            System.out.println(" -> [ERROR] Invalid username or password.");
        }
    }
}