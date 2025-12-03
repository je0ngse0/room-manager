package com.example.room_manager.repository;

import com.example.room_manager.domain.Room;
import com.example.room_manager.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByOwner(User owner);
}
