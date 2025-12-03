package com.example.room_manager.service;

import com.example.room_manager.domain.*;
import com.example.room_manager.dto.AvailabilityDto;
import com.example.room_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityGroupService {

    private final AvailabilityGroupRepository groupRepository;
    private final AvailabilityGroupDetailRepository detailRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public AvailabilityGroup createOrUpdateGroup(Long roomId, List<AvailabilityDto.Detail> details) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        AvailabilityGroup group = new AvailabilityGroup();
        group.setRoom(room);
        group = groupRepository.save(group);

        final AvailabilityGroup groupFinal = group;
        List<AvailabilityGroupDetail> detailEntities = details.stream().map(d -> {
            AvailabilityGroupDetail detail = new AvailabilityGroupDetail();
            detail.setGroup(groupFinal);
            detail.setWeekday(d.getWeekday());
            detail.setOpenTime(d.getOpenTime());
            detail.setCloseTime(d.getCloseTime());
            return detail;
        }).collect(Collectors.toList());

        detailRepository.saveAll(detailEntities);
        group.setDetails(detailEntities);
        return group;
    }

    public List<AvailabilityGroupDetail> getGroupDetails(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.getOwner() == null || room.getOwner().getRole() != User.Role.OWNER) {
            throw new IllegalArgumentException("User is not OWNER");
        }

        // 실제로 AvailabilityGroupDetail 가져오는 로직
        return room.getOwner().getRole() == User.Role.OWNER ?
                room.getOwner().getRole() != null ? List.of() : List.of() : List.of();
    }
}
