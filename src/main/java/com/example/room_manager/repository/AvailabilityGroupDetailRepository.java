package com.example.room_manager.repository;

import com.example.room_manager.domain.AvailabilityGroupDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvailabilityGroupDetailRepository extends JpaRepository<AvailabilityGroupDetail, Long> {
    @Query("""
        SELECT d
        FROM AvailabilityGroupDetail d
        WHERE d.group.room.roomId = :roomId
          AND d.weekday = :weekday
    """)
    List<AvailabilityGroupDetail> findByRoomIdAndWeekday(
            @Param("roomId") Long roomId,
            @Param("weekday") int weekday
    );
}