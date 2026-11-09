package com.torion.ui;

import com.torion.dao.ActivityLogDao;
import com.torion.dao.ParkingSlotDao;
import com.torion.dao.ParkingTicketDao;
import com.torion.model.ParkingSlot;
import com.torion.model.ParkingTicket;
import com.torion.model.User;

import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private final Scanner scanner;

    public CustomerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display(User customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==================================================");
            System.out.println("               CUSTOMER PORTAL                    ");
            System.out.println("==================================================");
            System.out.println("1. View Real-Time Slot Availability");
            System.out.println("2. Check Active Parking Status by Plate Number");
            System.out.println("3. Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> displaySlotMap(ParkingSlotDao.getAllSlots());
                case "2" -> {
                    String plate = promptInputOrCancel(scanner, "Enter Plate Number to search");
                    if (plate == null) {
                        System.out.println(" -> Operation cancelled.");
                        break;
                    }
                    plate = plate.toUpperCase();

                    List<ParkingTicket> tickets = ParkingTicketDao.getActiveTickets();

                    boolean found = false;
                    System.out.println("\n--- SEARCH RESULTS FOR PLATE: " + plate + " ---");
                    for (ParkingTicket t : tickets) {
                        if (t.plate_number().equalsIgnoreCase(plate)) {
                            System.out.println(" -> " + t);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println(" -> No active parking record found for plate: " + plate);
                    }
                }
                case "3" -> {
                    ActivityLogDao.logAction(customer.user_id(), "USER_LOGOUT", "Customer " + customer.username() + " logged out.");
                    loggedIn = false;
                    System.out.println("-> Logged out successfully. Returning to main menu...");
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void displaySlotMap(List<ParkingSlot> slots) {
        System.out.println("\n============================= LIVE PARKING AVAILABILITY =============================");
        if (slots.isEmpty()) {
            System.out.println(" -> No Parking Slots Found.");
            return;
        }
        System.out.println("+---------+-----------+-------------+---------------+------------------+");
        System.out.printf("| %-7s | %-9s | %-11s | %-13s | %-16s |\n", "Slot ID", "Slot Code", "Floor Level", "Vehicle Type", "Status");
        System.out.println("+---------+-----------+-------------+---------------+------------------+");
        for (ParkingSlot s : slots) {
            System.out.printf("| %-7d | %-9s | %-11d | %-13s | %-16s |\n",
                    s.slot_id(), s.slot_code(), s.floor_level(), s.slot_type(), s.status());
        }
        System.out.println("+---------+-----------+-------------+---------------+------------------+");
    }

    // ... your existing code and menu methods ...

    // --- PASTE THESE HELPER METHODS AT THE BOTTOM OF THE CLASS ---

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