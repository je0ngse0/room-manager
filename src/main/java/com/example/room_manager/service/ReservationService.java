package com.example.room_manager.service;

import com.example.room_manager.domain.*;
import com.example.room_manager.dto.ReservationDto;
import com.example.room_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    private static final long LOCK_EXPIRE = 5; // seconds

    @Transactional
    public Reservation createReservation(Long roomId, Long userId, ReservationDto dto) {

        String lockKey = "lock:room:" + roomId + ":" + dto.getDate() + ":" + dto.getStartTime();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_EXPIRE));

        if (locked == null || !locked) {
            throw new IllegalStateException("Another reservation is in progress");
        }

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // 운영시간 체크 / SpecialOverride 체크는 나중 구현 가능

            List<Reservation> existing = reservationRepository.findByRoomAndDate(room, dto.getDate());
            for (Reservation r : existing) {
                if (!(dto.getEndTime().isBefore(r.getStartTime()) || dto.getStartTime().isAfter(r.getEndTime()))) {
                    throw new IllegalArgumentException("Time slot already reserved");
                }
            }

            Reservation reservation = Reservation.builder()
                    .room(room)
                    .user(user)
                    .date(dto.getDate())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .status(Reservation.Status.RESERVED)
                    .build();

            return reservationRepository.save(reservation);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
