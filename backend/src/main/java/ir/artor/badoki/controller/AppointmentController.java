package ir.artor.badoki.controller;

import ir.artor.badoki.dto.AppointmentRequest;
import ir.artor.badoki.dto.AppointmentResponse;
import ir.artor.badoki.dto.RescheduleRequest;
import ir.artor.badoki.model.User;
import ir.artor.badoki.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> list(@AuthenticationPrincipal User user,
                                          @RequestParam(required = false) String filter) {
        return appointmentService.listForUser(user, filter);
    }

    @PostMapping
    public AppointmentResponse create(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody AppointmentRequest request) {
        return appointmentService.create(user, request);
    }

    @PutMapping("/{id}")
    public AppointmentResponse reschedule(@AuthenticationPrincipal User user,
                                          @PathVariable Long id,
                                          @Valid @RequestBody RescheduleRequest request) {
        return appointmentService.reschedule(user, id, request);
    }

    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return appointmentService.cancel(user, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        appointmentService.delete(user, id);
    }
}
