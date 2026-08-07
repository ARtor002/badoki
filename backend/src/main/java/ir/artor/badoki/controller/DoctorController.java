package ir.artor.badoki.controller;

import ir.artor.badoki.dto.DoctorResponse;
import ir.artor.badoki.dto.PagedResponse;
import ir.artor.badoki.dto.SlotDto;
import ir.artor.badoki.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/doctors")
    public PagedResponse<DoctorResponse> list(@RequestParam(required = false) String query,
                                              @RequestParam(required = false) String specialty,
                                              @RequestParam(required = false) String city,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return doctorService.search(query, specialty, city, page, size);
    }

    @GetMapping("/doctors/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @GetMapping("/doctors/{id}/slots")
    public List<SlotDto> slots(@PathVariable Long id,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return doctorService.slots(id, date);
    }

    @GetMapping("/meta/specialties")
    public List<String> specialties() {
        return doctorService.specialties();
    }

    @GetMapping("/meta/cities")
    public List<String> cities() {
        return doctorService.cities();
    }
}
