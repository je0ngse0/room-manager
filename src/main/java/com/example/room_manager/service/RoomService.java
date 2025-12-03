package com.example.room_manager.service;

import com.example.room_manager.domain.Room;
import com.example.room_manager.domain.User;
import com.example.room_manager.repository.RoomRepository;
import com.example.room_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public Room createRoom(Long ownerId, String name, String description) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        if(owner.getRole() != User.Role.OWNER) {
            throw new IllegalArgumentException("User is not OWNER");
        }

        Room room = Room.builder()
                .owner(owner)
                .name(name)
                .description(description)
                .build();
        return roomRepository.save(room);
    }
}
