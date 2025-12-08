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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final AvailabilityGroupService availabilityGroupService;
    private final SpecialOverrideService specialOverrideService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        User user = userService.signup(req.getEmail(), req.getPassword(), req.getName(), req.getPhone(), req.getRole());
        return ResponseEntity.ok(new SignupResponse(user.getUserId(), user.getEmail(), user.getRole()));
    }

    @PostMapping("login")
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
}
