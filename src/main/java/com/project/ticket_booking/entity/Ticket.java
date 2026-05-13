package com.project.ticket_booking.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private String id;
    private String userId;
    private String source;
    private String destination;
    private String dateOfTravel;
    private String trainId;
    private Integer row;
    private Integer seat;
}
