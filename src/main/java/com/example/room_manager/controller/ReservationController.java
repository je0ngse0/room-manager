package com.example.room_manager.controller;

import com.example.room_manager.domain.AvailabilityGroup;
import com.example.room_manager.domain.Reservation;
import com.example.room_manager.domain.Room;
import com.example.room_manager.domain.SpecialOverride;
import com.example.room_manager.dto.AvailabilityDto;
import com.example.room_manager.dto.OverrideDto;
import com.example.room_manager.dto.ReservationDto;
import com.example.room_manager.security.JwtTokenProvider;
import com.example.room_manager.service.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final RoomService roomService;
    private final ReservationService reservationService;
    private final AvailabilityGroupService availabilityGroupService;
    private final SpecialOverrideService specialOverrideService;

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody ReservationController.CreateRoomRequest req, @RequestParam Long ownerId) {
        Room room = roomService.createRoom(ownerId, req.getName(), req.getDescription());
        return ResponseEntity.ok(new ReservationController.CreateRoomResponse(room.getRoomId(), room.getOwner().getUserId()));
    }

    @Data
    static class CreateRoomRequest {
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    static class CreateRoomResponse {
        private Long roomId;
        private Long ownerId;
    }

    @PostMapping("/rooms/{roomId}/availability")
    public ResponseEntity<?> createAvailability(@PathVariable Long roomId, @RequestBody AvailabilityDto dto) {
        AvailabilityGroup group = availabilityGroupService.createOrUpdateGroup(roomId, dto.getDetails());
        return ResponseEntity.ok(group);
    }

    @PostMapping("/rooms/{roomId}/override")
    public ResponseEntity<?> createOverride(@PathVariable Long roomId, @RequestBody OverrideDto dto) {
        SpecialOverride override = specialOverrideService.createOverride(roomId, dto);
        return ResponseEntity.ok(override);
    }

    @PostMapping("/rooms/{roomId}/reservation")
    public ResponseEntity<?> createReservation(@PathVariable Long roomId,
                                               @RequestParam Long userId,
                                               @RequestBody ReservationDto dto) {
        Reservation res = reservationService.createReservation(roomId, userId, dto);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long id,
            @RequestHeader("USER-ID") Long requesterId) {

        Reservation r = reservationService.cancelReservation(id, requesterId);
        return ResponseEntity.ok(r);
    }

    /** 전체 방 목록 **/
    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /** 특정 OWNER의 방 목록 **/
    @GetMapping("/rooms/my")
    public ResponseEntity<?> getMyRooms(@RequestHeader("X-USER-ID") Long ownerId) {
        return ResponseEntity.ok(roomService.getRoomsByOwner(ownerId));
    }

    /** 날짜 필터 (YYYY-MM-DD) **/
    @GetMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<?> getRoomReservations(
            @PathVariable Long roomId,
            @RequestParam(required = false) String date) {

        if (date == null) {
            return ResponseEntity.ok(reservationService.getReservationsByRoom(roomId));
        }

        return ResponseEntity.ok(
                reservationService.getReservationsByRoomAndDate(roomId, LocalDate.parse(date))
        );
    }

    /** 특정 user의 예약 목록 **/
    @GetMapping("/users/me/reservations")
    public ResponseEntity<?> getMyReservations(
            @RequestHeader("USER-ID") Long userId) {

        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

}
