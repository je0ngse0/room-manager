package com.example.room_manager.repository;

import com.example.room_manager.domain.Room;
import com.example.room_manager.domain.SpecialOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpecialOverrideRepository extends JpaRepository<SpecialOverride, Long> {
    List<SpecialOverride> findByRoomAndDate(Room room, LocalDate date);
}
