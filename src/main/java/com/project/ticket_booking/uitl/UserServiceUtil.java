package com.project.ticket_booking.uitl;

import com.project.ticket_booking.entity.Train;
import com.project.ticket_booking.service.TrainService;

import java.util.List;

public class UserServiceUtil
{
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService
        }
    }
}
