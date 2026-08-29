package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.DoctorProfileUpdateRequest;
import ir.artor.badoki.dto.DoctorRequest;
import ir.artor.badoki.dto.DoctorResponse;
import ir.artor.badoki.dto.PagedResponse;
import ir.artor.badoki.dto.SlotDto;
import ir.artor.badoki.model.Appointment;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Doctor;
import ir.artor.badoki.model.Role;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.AppointmentRepository;
import ir.artor.badoki.repository.DoctorRepository;
import ir.artor.badoki.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PagedResponse<DoctorResponse> search(String query, String specialty, String city,
                                                String hospital, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<Doctor> result = doctorRepository.search(
                blankToNull(query),
                blankToNull(specialty),
                blankToNull(city),
                blankToNull(hospital),
                pageable);
        return PagedResponse.of(result,
                result.getContent().stream().map(DoctorResponse::from).toList());
    }

    public DoctorResponse get(Long id) {
        return DoctorResponse.from(findDoctor(id));
    }

    public List<String> specialties() {
        return doctorRepository.findDistinctSpecialties();
    }

    public List<String> cities() {
        return doctorRepository.findDistinctCities();
    }

    public List<String> hospitals() {
        return doctorRepository.findDistinctHospitals();
    }

    public long count() {
        return doctorRepository.count();
    }

    /** ساعات خالی و پر پزشک در یک روز مشخص */
    public List<SlotDto> slots(Long doctorId, LocalDate date) {
        Doctor doctor = findDoctor(doctorId);
        if (date.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "تاریخ انتخابی در گذشته است");
        }
        if (date.isAfter(LocalDate.now().plusDays(30))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "رزرو فقط تا ۳۰ روز آینده ممکن است");
        }
        if (!doctor.availableOn(date)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "پزشک در این روز ویزیت ندارد");
        }
        Set<LocalTime> booked = new HashSet<>();
        for (Appointment a : appointmentRepository
                .findByDoctorIdAndDateAndStatusNot(doctorId, date, AppointmentStatus.CANCELED)) {
            booked.add(a.getTime());
        }
        List<SlotDto> slots = new ArrayList<>();
        LocalTime t = LocalTime.of(doctor.getStartHour(), 0);
        LocalTime end = LocalTime.of(doctor.getEndHour(), 0);
        while (t.isBefore(end)) {
            slots.add(new SlotDto(t.toString(), !booked.contains(t)));
            t = t.plusMinutes(doctor.getSlotMinutes());
        }
        return slots;
    }

    // ---------- مدیریت (ادمین) ----------

    /**
     * ساخت پزشک توسط ادمین.
     * اگر «ایمیل» داده شود، حساب کاربری با نقش DOCTOR ساخته می‌شود و به پروفایل متصل می‌گردد.
     */
    @Transactional
    public DoctorResponse create(DoctorRequest req) {
        validateDays(req);
        Doctor d = new Doctor();
        apply(d, req);
        linkAccount(d, req);
        doctorRepository.save(d);
        return DoctorResponse.from(d);
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorRequest req) {
        validateDays(req);
        Doctor d = findDoctor(id);
        apply(d, req);
        doctorRepository.save(d);
        return DoctorResponse.from(d);
    }

    @Transactional
    public void delete(Long id) {
        Doctor d = findDoctor(id);
        appointmentRepository.deleteAll(
                appointmentRepository.findByDoctorIdAndDateAndStatusNot(id, LocalDate.now(), AppointmentStatus.CANCELED));
        doctorRepository.delete(d);
    }

    // ---------- پروفایل پزشک (نقش DOCTOR) ----------

    /** پروفایل پزشک متصل به حساب کاربری داده‌شده */
    public Doctor findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "حساب شما به پروفایل پزشک متصل نیست. با ادمین تماس بگیرید"));
    }

    /** ویرایش پروفایل توسط خود پزشک — نام، تخصص و شهر فقط در اختیار ادمین است */
    @Transactional
    public DoctorResponse updateByDoctor(Long userId, DoctorProfileUpdateRequest req) {
        validateDaysList(req.getAvailableDays(), req.getStartHour(), req.getEndHour());
        Doctor d = findByUserId(userId);
        d.setHospitalName(req.getHospitalName());
        d.setAddress(req.getAddress());
        d.setBio(req.getBio());
        d.setVisitPrice(req.getVisitPrice());
        d.setStartHour(req.getStartHour());
        d.setEndHour(req.getEndHour());
        d.setSlotMinutes(req.getSlotMinutes());
        d.setAvailableDaysList(req.getAvailableDays());
        doctorRepository.save(d);
        return DoctorResponse.from(d);
    }

    // ---------- ابزارها ----------

    private void linkAccount(Doctor d, DoctorRequest req) {
        String email = req.getEmail();
        if (email == null || email.isBlank()) return;
        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setFullName(req.getFullName().trim());
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(
                    (req.getPassword() == null || req.getPassword().isBlank()) ? "123456" : req.getPassword()));
            user.setRole(Role.DOCTOR);
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
        } else if (user.getRole() != Role.DOCTOR) {
            // ارتقای نقش کاربر موجود به پزشک
            user.setRole(Role.DOCTOR);
            userRepository.save(user);
        }
        d.setUserId(user.getId());
    }

    private void apply(Doctor d, DoctorRequest req) {
        d.setFullName(req.getFullName().trim());
        d.setSpecialty(req.getSpecialty().trim());
        d.setCity(req.getCity().trim());
        d.setHospitalName(req.getHospitalName());
        d.setAddress(req.getAddress());
        d.setBio(req.getBio());
        d.setVisitPrice(req.getVisitPrice());
        d.setExperienceYears(req.getExperienceYears());
        d.setRating(req.getRating());
        d.setReviewCount(req.getReviewCount());
        d.setAvailableDaysList(req.getAvailableDays());
        d.setStartHour(req.getStartHour());
        d.setEndHour(req.getEndHour());
        d.setSlotMinutes(req.getSlotMinutes());
    }

    private void validateDays(DoctorRequest req) {
        validateDaysList(req.getAvailableDays(), req.getStartHour(), req.getEndHour());
    }

    private void validateDaysList(List<String> days, int startHour, int endHour) {
        for (String day : days) {
            try {
                DayOfWeek.valueOf(day.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "نام روز نامعتبر است: " + day);
            }
        }
        if (startHour >= endHour) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ساعت پایان باید بزرگ‌تر از ساعت شروع باشد");
        }
    }

    Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "پزشک پیدا نشد"));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
