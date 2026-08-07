package ir.artor.badoki.controller;

import ir.artor.badoki.dto.AdminStatsResponse;
import ir.artor.badoki.dto.AdminUserResponse;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Role;
import ir.artor.badoki.repository.AppointmentRepository;
import ir.artor.badoki.repository.DoctorRepository;
import ir.artor.badoki.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** مدیریت کاربران و آمار — فقط نقش ADMIN */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminUserController(UserRepository userRepository,
                               DoctorRepository doctorRepository,
                               AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(AdminUserResponse::from)
                .toList();
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        long pending = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        return new AdminStatsResponse(
                doctorRepository.count(),
                userRepository.countByRole(Role.PATIENT),
                appointmentRepository.count(),
                pending);
    }
}
