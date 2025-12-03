package com.example.room_manager.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "special_override")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overrideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate date;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
}
