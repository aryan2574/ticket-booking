package com.project.ticket_booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ticket_booking.entity.Ticket;
import com.project.ticket_booking.entity.Train;
import com.project.ticket_booking.entity.User;
import com.project.ticket_booking.uitl.UserServiceUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserBookingService {

    private User loggedInUser;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERS_PATH = "localdb/users.json";

    public UserBookingService() throws IOException {
        userList = objectMapper.readValue(new File(USERS_PATH), new TypeReference<List<User>>() {});
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    public boolean loginUser(String name, String password) {
        Optional<User> foundUser = userList.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst();

        if (foundUser.isPresent() && UserServiceUtil.checkPassword(password, foundUser.get().getHashedPassword())) {
            this.loggedInUser = foundUser.get();
            return true;
        }
        return false;
    }

    public boolean signupUser(String name, String password) throws IOException {
        boolean exists = userList.stream().anyMatch(u -> u.getName().equals(name));
        if (exists) return false;

        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setName(name);
        newUser.setHashedPassword(UserServiceUtil.hashPassword(password));
        newUser.setTicketsBooked(new ArrayList<>());

        userList.add(newUser);
        saveUsersToFile();
        return true;
    }

    // ── BOOKINGS ──────────────────────────────────────────────────────────────

    public List<Ticket> fetchBookings() {
        return loggedInUser.getTicketsBooked();
    }

    public Ticket bookTicket(String source, String destination, String dateOfTravel,
                             Train train, int row, int seat) throws IOException {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setUserId(loggedInUser.getId());
        ticket.setSource(source);
        ticket.setDestination(destination);
        ticket.setDateOfTravel(dateOfTravel);
        ticket.setTrainId(train.getId());
        ticket.setRow(row);
        ticket.setSeat(seat);

        loggedInUser.getTicketsBooked().add(ticket);
        updateLoggedInUser();
        saveUsersToFile();
        return ticket;
    }

    public boolean cancelBooking(String ticketId) throws IOException {
        List<Ticket> tickets = loggedInUser.getTicketsBooked();
        Optional<Ticket> toCancel = tickets.stream()
                .filter(t -> t.getId().equals(ticketId))
                .findFirst();

        if (toCancel.isPresent()) {
            tickets.remove(toCancel.get());
            updateLoggedInUser();
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // ── TICKET DOWNLOAD ───────────────────────────────────────────────────────

    public void downloadTicket(Ticket ticket) throws IOException {
        new File("tickets").mkdirs();
        String filePath = "tickets/ticket-" + ticket.getId() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("===============================\n");
            writer.write("         BOOKING CONFIRMED     \n");
            writer.write("===============================\n");
            writer.write("Ticket ID  : " + ticket.getId() + "\n");
            writer.write("Passenger  : " + loggedInUser.getName() + "\n");
            writer.write("From       : " + ticket.getSource() + "\n");
            writer.write("To         : " + ticket.getDestination() + "\n");
            writer.write("Date       : " + ticket.getDateOfTravel() + "\n");
            writer.write("Train ID   : " + ticket.getTrainId() + "\n");
            writer.write("Seat       : Row " + ticket.getRow() + ", Seat " + ticket.getSeat() + "\n");
            writer.write("===============================\n");
        }
        System.out.println("Ticket downloaded to: " + filePath);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private void updateLoggedInUser() {
        userList = userList.stream()
                .map(u -> u.getId().equals(loggedInUser.getId()) ? loggedInUser : u)
                .collect(Collectors.toList());
    }

    private void saveUsersToFile() throws IOException {
        objectMapper.writeValue(new File(USERS_PATH), userList);
    }
}
