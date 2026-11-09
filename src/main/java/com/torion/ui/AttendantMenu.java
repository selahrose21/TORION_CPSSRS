package com.torion.ui;

import com.torion.dao.ActivityLogDao;
import com.torion.dao.ParkingSlotDao;
import com.torion.dao.ParkingTicketDao;
import com.torion.model.ParkingSlot;
import com.torion.model.ParkingTicket;
import com.torion.model.User;

import java.util.List;
import java.util.Scanner;

public class AttendantMenu {
    private final Scanner scanner;

    public AttendantMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display(User attendant) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==================================================");
            System.out.println("               ATTENDANT BOOTH DASHBOARD           ");
            System.out.println("==================================================");
            System.out.println("1. View Parking Slot Map");
            System.out.println("2. Check-In Vehicle (Create Ticket)");
            System.out.println("3. View Active Parking Tickets");
            System.out.println("4. Check-Out Vehicle (Process Cash Payment)");
            System.out.println("5. Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> displaySlotMap(ParkingSlotDao.getAllSlots());
                case "2" -> {
                    displaySlotMap(ParkingSlotDao.getAllSlots());

                    // Use the helper method and check for cancellation (null)
                    String plateNumber = promptInputOrCancel(scanner, "Enter Plate Number");
                    if (plateNumber == null) {
                        System.out.println(" -> Operation cancelled.");
                        break;
                    }

                    // Use the helper method and check for cancellation (-1)
                    int slotId = promptIntOrCancel(scanner, "Enter Target Slot ID to occupy");
                    if (slotId == -1) {
                        System.out.println(" -> Operation cancelled.");
                        break;
                    }

                    boolean success = ParkingTicketDao.checkInVehicle(attendant.user_id(), plateNumber, slotId);
                    if (success) {
                        System.out.println("[SUCCESS] Vehicle checked in successfully!");
                    } else {
                        System.out.println("[ERROR] Failed to check in vehicle.");
                    }
                }
                case "3" -> displayActiveTicketTable(ParkingTicketDao.getActiveTickets(), "ACTIVE PARKING TICKETS");
                case "4" -> {
                    List<ParkingTicket> activeTickets = ParkingTicketDao.getActiveTickets();
                    displayActiveTicketTable(activeTickets, "ACTIVE TICKETS FOR CHECKOUT");

                    if (activeTickets.isEmpty()) break;

                    int ticketId = promptIntOrCancel(scanner, "Enter Ticket ID to check out");
                    if (ticketId == -1) {
                        System.out.println(" -> Operation cancelled.");
                        break;
                    }

                    int slotId = promptIntOrCancel(scanner, "Enter corresponding Slot ID being vacated");
                    if (slotId == -1) {
                        System.out.println(" -> Operation cancelled.");
                        break;
                    }

                    boolean success = ParkingTicketDao.checkOutVehicle(attendant.user_id(), ticketId, slotId);
                    if (success) {
                        System.out.println("[SUCCESS] Checkout processed, payment received, slot is now VACANT!");
                    } else {
                        System.out.println("[ERROR] Failed to process checkout.");
                    }
                }
                case "5" -> {
                    ActivityLogDao.logAction(attendant.user_id(), "USER_LOGOUT", "Attendant " + attendant.username() + " logged out.");
                    loggedIn = false;
                    System.out.println("-> Logged out successfully. Returning to main menu...");
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void displaySlotMap(List<ParkingSlot> slots) {
        System.out.println("\n============================= PARKING SLOT MAP =============================");
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

    private void displayActiveTicketTable(List<ParkingTicket> tickets, String title) {
        System.out.println("\n============================= " + title + " =============================");
        if (tickets.isEmpty()) {
            System.out.println(" -> No Active Tickets Found.");
            return;
        }
        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
        System.out.printf("| %-9s | %-7s | %-12s | %-19s | %-14s |\n", "Ticket ID", "Slot ID", "Plate Number", "Entry Time", "Status");
        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
        for (ParkingTicket t : tickets) {
            System.out.printf("| %-9d | %-7d | %-12s | %-19s | %-14s |\n",
                    t.ticket_id(), t.slot_id(), t.plate_number(), t.entry_time(), t.payment_status());
        }
        System.out.println("+-----------+---------+--------------+---------------------+----------------+");
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