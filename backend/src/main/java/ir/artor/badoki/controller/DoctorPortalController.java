package ir.artor.badoki.controller;

import ir.artor.badoki.dto.AppointmentResponse;
import ir.artor.badoki.dto.DoctorDashboardResponse;
import ir.artor.badoki.dto.DoctorProfileUpdateRequest;
import ir.artor.badoki.dto.DoctorResponse;
import ir.artor.badoki.dto.StatusUpdateRequest;
import ir.artor.badoki.model.Doctor;
import ir.artor.badoki.model.User;
import ir.artor.badoki.service.AppointmentService;
import ir.artor.badoki.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** پورتال پزشک — فقط نقش DOCTOR */
@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorPortalController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorPortalController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    private Doctor myProfile(User user) {
        return doctorService.findByUserId(user.getId());
    }

    @GetMapping("/me")
    public DoctorResponse me(@AuthenticationPrincipal User user) {
        return DoctorResponse.from(myProfile(user));
    }

    @PutMapping("/me")
    public DoctorResponse updateMe(@AuthenticationPrincipal User user,
                                   @Valid @RequestBody DoctorProfileUpdateRequest request) {
        return doctorService.updateByDoctor(user.getId(), request);
    }

    @GetMapping("/dashboard")
    public DoctorDashboardResponse dashboard(@AuthenticationPrincipal User user) {
        return appointmentService.doctorDashboard(myProfile(user));
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments(@AuthenticationPrincipal User user,
                                                  @RequestParam(required = false) String filter) {
        return appointmentService.listForDoctor(myProfile(user), filter);
    }

    @PutMapping("/appointments/{id}/status")
    public AppointmentResponse updateStatus(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest request) {
        return appointmentService.updateStatusForDoctor(myProfile(user), id, request.getStatus());
    }
}
