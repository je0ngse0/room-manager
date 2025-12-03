package com.example.room_manager.service;

import com.example.room_manager.domain.*;
import com.example.room_manager.dto.OverrideDto;
import com.example.room_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialOverrideService {

    private final SpecialOverrideRepository overrideRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public SpecialOverride createOverride(Long roomId, OverrideDto dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        SpecialOverride override = SpecialOverride.builder()
                .room(room)
                .date(dto.getDate())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .isClosed(dto.getIsClosed())
                .build();

        return overrideRepository.save(override);
    }

    public List<SpecialOverride> getOverrides(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return overrideRepository.findByRoomAndDate(room, null); // date 필터는 API에서 옵션 처리 가능
    }
}

