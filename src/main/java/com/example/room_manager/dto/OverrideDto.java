package com.example.room_manager.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OverrideDto {

    private Long overrideId;
    private LocalDate date;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
}
