package com.project.ticket_booking.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Train {
    private String id;
    private String number;
    private List<List<Integer>> seats;
    private Map<String, String> stationTimes;
    private List<String> stations;
}
