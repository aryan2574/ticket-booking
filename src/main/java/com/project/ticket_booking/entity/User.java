package com.project.ticket_booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    @JsonIgnore
    private String password;
    private String hashedPassword;
    private List<Ticket> ticketsBooked;
}
