package com.example.room_manager.service;

import com.example.room_manager.domain.*;
import com.example.room_manager.dto.ReservationDto;
import com.example.room_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final AvailabilityGroupDetailRepository availabilityGroupDetailRepository;

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

            int weekday = dto.getDate().getDayOfWeek().getValue();
            LocalTime start = dto.getStartTime();
            LocalTime end = dto.getEndTime();

            validateAvailability(roomId, weekday, start, end);

            List<Reservation> existing = reservationRepository.findByRoomAndDate(room, dto.getDate());
            for (Reservation r : existing) {
                boolean overlaps =
                        dto.getStartTime().isBefore(r.getEndTime()) &&
                                dto.getEndTime().isAfter(r.getStartTime());

                if (overlaps) {
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

    private void validateAvailability(Long roomId, int weekday, LocalTime start, LocalTime end) {

        List<AvailabilityGroupDetail> details =
                availabilityGroupDetailRepository.findByRoomIdAndWeekday(roomId, weekday);

        if (details.isEmpty()) {
            throw new IllegalStateException("No availability for this weekday");
        }

        boolean fits = details.stream().anyMatch(d ->
                !start.isBefore(d.getOpenTime()) && !end.isAfter(d.getCloseTime())
        );

        if (!fits) {
            throw new IllegalStateException("Reservation time is outside availability");
        }
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, Long requesterId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isOwner = reservation.getRoom().getOwner().getUserId().equals(requesterId);
        boolean isUser = reservation.getUser().getUserId().equals(requesterId);

        if (!isOwner && !isUser) {
            throw new IllegalStateException("You cannot cancel this reservation");
        }

        reservation.setStatus(Reservation.Status.CANCELED);
        reservation.setUpdatedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return reservationRepository.findByRoom(room);
    }

    public List<Reservation> getReservationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> getReservationsByRoomAndDate(Long roomId, LocalDate date) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return reservationRepository.findByRoomAndDate(room, date);
    }


}
