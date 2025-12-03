package com.example.room_manager.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationDto {

    private Long reservationId;
    private Long userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
