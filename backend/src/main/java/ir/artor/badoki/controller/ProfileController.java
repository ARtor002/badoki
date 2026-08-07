package ir.artor.badoki.controller;

import ir.artor.badoki.dto.DashboardResponse;
import ir.artor.badoki.dto.ProfileUpdateRequest;
import ir.artor.badoki.dto.UserResponse;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.UserRepository;
import ir.artor.badoki.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final UserRepository userRepository;
    private final AppointmentService appointmentService;

    public ProfileController(UserRepository userRepository, AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return toResponse(user);
    }

    @PutMapping("/me")
    public UserResponse update(@AuthenticationPrincipal User user,
                               @Valid @RequestBody ProfileUpdateRequest request) {
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return toResponse(user);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@AuthenticationPrincipal User user) {
        return appointmentService.dashboard(user);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
    }
}
