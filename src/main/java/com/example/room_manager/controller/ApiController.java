package com.example.room_manager.controller;

import com.example.room_manager.domain.*;
import com.example.room_manager.dto.AvailabilityDto;
import com.example.room_manager.dto.LoginRequest;
import com.example.room_manager.dto.OverrideDto;
import com.example.room_manager.dto.ReservationDto;
import com.example.room_manager.security.JwtTokenProvider;
import com.example.room_manager.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final AvailabilityGroupService availabilityGroupService;
    private final SpecialOverrideService specialOverrideService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        User user = userService.signup(req.getEmail(), req.getPassword(), req.getName(), req.getPhone(), req.getRole());
        return ResponseEntity.ok(new SignupResponse(user.getUserId(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userService.getByEmail(req.getEmail());

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }


    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest req, @RequestParam Long ownerId) {
        Room room = roomService.createRoom(ownerId, req.getName(), req.getDescription());
        return ResponseEntity.ok(new CreateRoomResponse(room.getRoomId(), room.getOwner().getUserId()));
    }

    @Data
    static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String phone;
        private User.Role role;
    }

    @Data
    @AllArgsConstructor
    static class SignupResponse {
        private Long userId;
        private String email;
        private User.Role role;
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
