package com.example.room_manager.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "availability_group_detail")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilityGroupDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private AvailabilityGroup group;

    private int weekday; // 0: Sunday, 1: Monday ...
    private LocalTime openTime;
    private LocalTime closeTime;
}
