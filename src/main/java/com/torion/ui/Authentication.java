package com.torion.ui;

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
            System.out.println("\n======================================");
            System.out.println("\t   PARKSPOT SYSTEM MENU              ");
            System.out.println("======================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    System.out.print("Enter full name: ");
                    String fname = sc.nextLine();
                    System.out.print("Enter username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter password: ");
                    String pass = sc.nextLine();

                    System.out.print("Select Role [1] Admin [2] Attendant [3] Customer: ");
                    int roleChoice = sc.nextInt();
                    sc.nextLine();

                    String role;
                    switch (roleChoice) {
                        case 1:
                            role = "ADMIN";
                            break;
                        case 2:
                            role = "ATTENDANT";
                            break;
                        case 3:
                            role = "CUSTOMER";
                            break;
                        default:
                            role = "null";

                    }

                    if (role != null) {

                        UserDao.registerUser(fname, user, pass, role);

                    } else {
                        System.out.println("Invalid role selection!");
                    }

                case "3":
                    System.out.println("Exiting application. Goodbye!");
                    break;

            }
        }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password_hash = sc.nextLine();

        User user = userDao.login(username, password_hash);

        if (user != null){
            System.out.println("/n ->Welcome " +user.username()+ " | ROLE: " +user.role()+"");
            switch(user.role()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                default -> System.out.println("Account role not found");
            }
        }


    }


}
