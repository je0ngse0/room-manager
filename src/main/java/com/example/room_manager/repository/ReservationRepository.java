package com.example.room_manager.repository;

import com.example.room_manager.domain.Reservation;
import com.example.room_manager.domain.Room;
import com.example.room_manager.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRoom(Room room);
    List<Reservation> findByUser(User user);
    List<Reservation> findByRoomAndDate(Room room, LocalDate date);
}
