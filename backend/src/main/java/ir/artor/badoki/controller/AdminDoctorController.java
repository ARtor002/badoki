package ir.artor.badoki.controller;

import ir.artor.badoki.dto.DoctorRequest;
import ir.artor.badoki.dto.DoctorResponse;
import ir.artor.badoki.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** مدیریت پزشکان — فقط ادمین */
@RestController
@RequestMapping("/api/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorResponse> findAll() {
        return doctorService.search(null, null, null, 0, 200).getItems();
    }

    @PostMapping
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return doctorService.create(request);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }
}
