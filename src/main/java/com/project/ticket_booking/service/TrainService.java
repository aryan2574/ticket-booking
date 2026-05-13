package com.project.ticket_booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ticket_booking.entity.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TrainService {

    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAINS_PATH = "localdb/trains.json";

    public TrainService() throws IOException {
        trainList = objectMapper.readValue(new File(TRAINS_PATH), new TypeReference<List<Train>>() {});
    }

    public List<Train> getTrainsBetweenStations(String source, String destination) {
        return trainList.stream()
                .filter(train -> {
                    List<String> stations = train.getStations();
                    int sourceIndex = stations.indexOf(source.toLowerCase());
                    int destinationIndex = stations.indexOf(destination.toLowerCase());
                    return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
                }).collect(Collectors.toList());
    }

    public void updateTrain(Train newTrain) throws IOException {
        List<Train> updatedList = trainList.stream().map(t -> t.getId().equals(newTrain.getId()) ? newTrain : t)
                .collect(Collectors.toList());

        objectMapper.writeValue(new File(TRAINS_PATH), updatedList);
        this.trainList = updatedList;
    }

    public List<Train> getAllTrains() {
        return trainList;
    }

    public int[] bookASeat(Train train) throws IOException {
        try {
            for (int row = 0; row < train.getSeats().size(); row++) {
                for (int seat = 0; seat < train.getSeats().get(row).size(); seat++) {
                    if (train.getSeats().get(row).get(seat) == 0) {
                        train.getSeats().get(row).set(seat, 1);
                        updateTrain(train);
                        return new int[]{row, seat};
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
