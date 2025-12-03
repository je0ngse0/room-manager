package com.example.room_manager.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Data
public class AvailabilityDto {

    private Long groupId;
    private List<Detail> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private int weekday; // 0: Sunday, 1: Monday ...
        private LocalTime openTime;
        private LocalTime closeTime;
    }
}
