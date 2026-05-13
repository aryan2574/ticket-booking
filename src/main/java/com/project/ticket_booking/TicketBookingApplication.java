package com.project.ticket_booking;

import com.project.ticket_booking.entity.Ticket;
import com.project.ticket_booking.entity.Train;
import com.project.ticket_booking.service.TrainService;
import com.project.ticket_booking.service.UserBookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class TicketBookingApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TicketBookingApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("================================");
        System.out.println("   Welcome to Train Booking     ");
        System.out.println("================================");

        Scanner scanner = new Scanner(System.in);
        showWelcomeMenu(scanner);
        scanner.close();
    }

    // ── WELCOME MENU ──────────────────────────────────────────────────────────

    private void showWelcomeMenu(Scanner scanner) throws Exception {
        while (true) {
            System.out.println("\n1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> handleSignup(scanner);
                case "2" -> {
                    if (handleLogin(scanner)) return; // moves to main menu inside handleLogin
                }
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2 or 3.");
            }
        }
    }

    // ── SIGNUP ────────────────────────────────────────────────────────────────

    private void handleSignup(Scanner scanner) throws Exception {
        System.out.print("Enter name     : ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter password : ");
        String password = scanner.nextLine().trim();

        UserBookingService service = new UserBookingService();
        boolean success = service.signupUser(name, password);

        if (success) {
            System.out.println("Account created! Please login.");
        } else {
            System.out.println("Username already exists. Try a different name.");
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    private boolean handleLogin(Scanner scanner) throws Exception {
        System.out.print("Enter name     : ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter password : ");
        String password = scanner.nextLine().trim();

        UserBookingService service = new UserBookingService();
        if (service.loginUser(name, password)) {
            System.out.println("\nWelcome, " + name + "!");
            showMainMenu(scanner, service);
            return true;
        } else {
            System.out.println("Invalid username or password.");
            return false;
        }
    }

    // ── MAIN MENU ─────────────────────────────────────────────────────────────

    private void showMainMenu(Scanner scanner, UserBookingService userService) throws Exception {
        while (true) {
            System.out.println("\n================================");
            System.out.println("1. Search & Book Trains");
            System.out.println("2. My Bookings");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Download Ticket");
            System.out.println("5. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> handleSearchAndBook(scanner, userService);
                case "2" -> handleViewBookings(userService);
                case "3" -> handleCancelBooking(scanner, userService);
                case "4" -> handleDownloadTicket(scanner, userService);
                case "5" -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ── SEARCH & BOOK ─────────────────────────────────────────────────────────

    private void handleSearchAndBook(Scanner scanner, UserBookingService userService) throws Exception {
        System.out.print("Source station      : ");
        String source = scanner.nextLine().trim().toLowerCase();
        System.out.print("Destination station : ");
        String destination = scanner.nextLine().trim().toLowerCase();
        System.out.print("Date of travel (YYYY-MM-DD) : ");
        String date = scanner.nextLine().trim();

        TrainService trainService = new TrainService();
        List<Train> trains = trainService.getTrainsBetweenStations(source, destination);

        if (trains.isEmpty()) {
            System.out.println("No trains found for " + source + " → " + destination);
            return;
        }

        // Display train options
        System.out.println("\nAvailable trains:");
        System.out.println("─────────────────────────────────────────");
        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            long freeSeats = t.getSeats().stream().flatMap(List::stream).filter(s -> s == 0).count();
            System.out.printf("%d. Train %-8s  %s → %s  [%d seats free]%n",
                    i + 1, t.getNumber(), source, destination, freeSeats);
        }
        System.out.println("─────────────────────────────────────────");
        System.out.print("Select train (0 to go back): ");

        int trainChoice;
        try {
            trainChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        if (trainChoice == 0) return;
        if (trainChoice < 1 || trainChoice > trains.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Train selectedTrain = trains.get(trainChoice - 1);

        // Display seat grid
        System.out.println("\nSeat map  ([ ] = free, [X] = taken):");
        List<List<Integer>> seats = selectedTrain.getSeats();
        System.out.print("   ");
        for (int col = 0; col < seats.get(0).size(); col++) System.out.printf("  %d", col);
        System.out.println();
        for (int row = 0; row < seats.size(); row++) {
            System.out.printf("%d  ", row);
            for (int col = 0; col < seats.get(row).size(); col++) {
                System.out.print(seats.get(row).get(col) == 0 ? "[ ]" : "[X]");
            }
            System.out.println();
        }

        System.out.print("\nPress ENTER to auto-book first available seat, or 0 to go back: ");
        if (scanner.nextLine().trim().equals("0")) return;

        // Book the seat
        int[] bookedSeat = trainService.bookASeat(selectedTrain);
        if (bookedSeat == null) {
            System.out.println("Sorry, no seats available on this train.");
            return;
        }

        Ticket ticket = userService.bookTicket(source, destination, date, selectedTrain, bookedSeat[0], bookedSeat[1]);

        System.out.println("\n✓ Booking Confirmed!");
        System.out.println("─────────────────────────────");
        System.out.println("Ticket ID  : " + ticket.getId());
        System.out.println("From       : " + ticket.getSource());
        System.out.println("To         : " + ticket.getDestination());
        System.out.println("Date       : " + ticket.getDateOfTravel());
        System.out.println("Train      : " + selectedTrain.getNumber());
        System.out.println("Seat       : Row " + ticket.getRow() + ", Seat " + ticket.getSeat());
        System.out.println("─────────────────────────────");

        System.out.print("Download ticket to file? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            userService.downloadTicket(ticket);
        }
    }

    // ── VIEW BOOKINGS ─────────────────────────────────────────────────────────

    private void handleViewBookings(UserBookingService userService) {
        List<Ticket> bookings = userService.fetchBookings();

        if (bookings.isEmpty()) {
            System.out.println("You have no bookings.");
            return;
        }

        System.out.println("\nYour Bookings:");
        System.out.println("─────────────────────────────────────────────────────────");
        for (int i = 0; i < bookings.size(); i++) {
            Ticket t = bookings.get(i);
            System.out.printf("%d. [%s]  %s → %s  Date: %s  Seat: Row%d Seat%d%n",
                    i + 1, t.getId().substring(0, 8) + "...",
                    t.getSource(), t.getDestination(),
                    t.getDateOfTravel(), t.getRow(), t.getSeat());
        }
        System.out.println("─────────────────────────────────────────────────────────");
    }

    // ── CANCEL BOOKING ────────────────────────────────────────────────────────

    private void handleCancelBooking(Scanner scanner, UserBookingService userService) throws Exception {
        handleViewBookings(userService);
        if (userService.fetchBookings().isEmpty()) return;

        System.out.print("Enter Ticket ID to cancel (or 0 to go back): ");
        String ticketId = scanner.nextLine().trim();
        if (ticketId.equals("0")) return;

        // Find the ticket so we can free the seat in trains.json
        Ticket toCancel = userService.fetchBookings().stream()
                .filter(t -> t.getId().startsWith(ticketId) || t.getId().equals(ticketId))
                .findFirst().orElse(null);

        if (toCancel == null) {
            System.out.println("Ticket not found.");
            return;
        }

        // Free the seat in trains.json
        TrainService trainService = new TrainService();
        trainService.getAllTrains().stream()
                .filter(t -> t.getId().equals(toCancel.getTrainId()))
                .findFirst()
                .ifPresent(train -> {
                    train.getSeats().get(toCancel.getRow()).set(toCancel.getSeat(), 0);
                    try { trainService.updateTrain(train); } catch (IOException e) { e.printStackTrace(); }
                });

        boolean cancelled = userService.cancelBooking(toCancel.getId());
        if (cancelled) {
            System.out.println("Booking cancelled successfully.");
        } else {
            System.out.println("Could not cancel booking.");
        }
    }

    // ── DOWNLOAD TICKET ───────────────────────────────────────────────────────

    private void handleDownloadTicket(Scanner scanner, UserBookingService userService) throws Exception {
        handleViewBookings(userService);
        if (userService.fetchBookings().isEmpty()) return;

        System.out.print("Enter Ticket ID to download (or 0 to go back): ");
        String ticketId = scanner.nextLine().trim();
        if (ticketId.equals("0")) return;

        Ticket ticket = userService.fetchBookings().stream()
                .filter(t -> t.getId().startsWith(ticketId) || t.getId().equals(ticketId))
                .findFirst().orElse(null);

        if (ticket == null) {
            System.out.println("Ticket not found.");
            return;
        }

        userService.downloadTicket(ticket);
    }
}
