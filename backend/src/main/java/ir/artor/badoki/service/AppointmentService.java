package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.AppointmentRequest;
import ir.artor.badoki.dto.AppointmentResponse;
import ir.artor.badoki.dto.DashboardResponse;
import ir.artor.badoki.dto.DoctorDashboardResponse;
import ir.artor.badoki.dto.RescheduleRequest;
import ir.artor.badoki.model.Appointment;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Doctor;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.AppointmentRepository;
import ir.artor.badoki.util.Jalali;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorService doctorService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.notificationService = notificationService;
    }

    /** فیلتر: all | upcoming | past | canceled */
    @Transactional
    public List<AppointmentResponse> listForUser(User user, String filter) {
        expireForUser(user.getId());
        List<Appointment> list;
        if (filter == null || filter.isBlank() || filter.equals("all")) {
            list = appointmentRepository.findByPatientIdOrderByDateDescTimeDesc(user.getId());
        } else if (filter.equals("upcoming")) {
            list = appointmentRepository.findByPatientIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
                    user.getId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                    LocalDate.now());
        } else if (filter.equals("past")) {
            list = appointmentRepository.findByPatientIdAndDateLessThanOrderByDateDescTimeDesc(
                    user.getId(), LocalDate.now());
        } else if (filter.equals("canceled")) {
            list = appointmentRepository.findByPatientIdAndStatusOrderByDateDescTimeDesc(
                    user.getId(), AppointmentStatus.CANCELED);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "فیلتر نامعتبر است. مقادیر مجاز: all, upcoming, past, canceled");
        }
        return list.stream().map(AppointmentResponse::from).toList();
    }

    @Transactional
    public AppointmentResponse create(User user, AppointmentRequest req) {
        Doctor doctor = doctorService.findDoctor(req.getDoctorId());
        LocalDate date = parseDate(req.getDate());
        LocalTime time = parseTime(req.getTime());
        validateSlot(doctor, date, time, null);
        if (appointmentRepository.existsByPatientIdAndDoctorIdAndDateAndTimeAndStatusNot(
                user.getId(), doctor.getId(), date, time, AppointmentStatus.CANCELED)) {
            throw new ApiException(HttpStatus.CONFLICT, "شما قبلاً برای همین زمان نوبت گرفته‌اید");
        }
        Appointment a = new Appointment();
        a.setPatient(user);
        a.setDoctor(doctor);
        a.setDate(date);
        a.setTime(time);
        // نوبت جدید ابتدا «در انتظار تأیید پزشک» است
        a.setStatus(AppointmentStatus.PENDING);
        a.setNotes(req.getNotes());
        appointmentRepository.save(a);
        return AppointmentResponse.from(a);
    }

    @Transactional
    public AppointmentResponse reschedule(User user, Long id, RescheduleRequest req) {
        Appointment a = findOwned(user, id);
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت انجام‌شده قابل تغییر نیست");
        }
        LocalDate date = parseDate(req.getDate());
        LocalTime time = parseTime(req.getTime());
        validateSlot(a.getDoctor(), date, time, a);
        a.setDate(date);
        a.setTime(time);
        a.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(a);
        return AppointmentResponse.from(a);
    }

    @Transactional
    public AppointmentResponse cancel(User user, Long id) {
        Appointment a = findOwned(user, id);
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت انجام‌شده قابل لغو نیست");
        }
        if (a.getStatus() == AppointmentStatus.CANCELED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "این نوبت قبلاً لغو شده است");
        }
        if (isPast(a)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت‌های گذشته قابل لغو نیستند");
        }
        a.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(a);

        // اطلاع‌رسانی به پزشک
        notificationService.notifyUser(a.getDoctor().getUserId(), "لغو نوبت توسط بیمار",
                "بیمار «" + user.getFullName() + "» نوبت " + a.getDate() + " ساعت " + a.getTime()
                        + " را لغو کرد.",
                "APPOINTMENT_CANCELED");
        return AppointmentResponse.from(a);
    }

    @Transactional
    public void delete(User user, Long id) {
        Appointment a = findOwned(user, id);
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت انجام‌شده قابل حذف نیست");
        }
        appointmentRepository.delete(a);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(User user) {
        DashboardResponse d = new DashboardResponse();
        List<AppointmentStatus> active = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        d.setUpcomingCount(appointmentRepository.countByPatientIdAndStatusIn(user.getId(), active));
        d.setCompletedCount(appointmentRepository.countByPatientIdAndStatusIn(
                user.getId(), List.of(AppointmentStatus.COMPLETED)));
        d.setCanceledCount(appointmentRepository.countByPatientIdAndStatusIn(
                user.getId(), List.of(AppointmentStatus.CANCELED)));
        d.setTotalDoctors(doctorService.count());
        appointmentRepository
                .findFirstByPatientIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
                        user.getId(), active, LocalDate.now())
                .ifPresent(a -> d.setNextAppointment(AppointmentResponse.from(a)));
        return d;
    }

    // ---------- سمت پزشک (نقش DOCTOR) ----------

    /** فیلتر: all | upcoming | past */
    @Transactional
    public List<AppointmentResponse> listForDoctor(Doctor doctor, String filter) {
        expireForDoctor(doctor.getId());
        List<Appointment> list;
        if (filter == null || filter.isBlank() || filter.equals("all")) {
            list = appointmentRepository.findByDoctorIdOrderByDateDescTimeDesc(doctor.getId());
        } else if (filter.equals("upcoming")) {
            list = appointmentRepository.findByDoctorIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
                    doctor.getId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                    LocalDate.now());
        } else if (filter.equals("past")) {
            list = appointmentRepository.findPastForDoctor(
                    doctor.getId(), LocalDate.now(), AppointmentStatus.COMPLETED);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "فیلتر نامعتبر است. مقادیر مجاز: all, upcoming, past");
        }
        return list.stream().map(AppointmentResponse::from).toList();
    }

    /** تغییر وضعیت نوبت توسط پزشک: تأیید، انجام‌شده یا لغو */
    @Transactional
    public AppointmentResponse updateStatusForDoctor(Doctor doctor, Long appointmentId, String status) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "نوبت پیدا نشد"));
        if (!a.getDoctor().getId().equals(doctor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "این نوبت متعلق به شما نیست");
        }
        AppointmentStatus target;
        try {
            target = AppointmentStatus.valueOf(status);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "وضعیت نامعتبر است");
        }
        if (target == AppointmentStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "وضعیت PENDING قابل انتخاب نیست");
        }
        AppointmentStatus current = a.getStatus();
        if (current == AppointmentStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت انجام‌شده قابل تغییر نیست");
        }
        if (current == AppointmentStatus.CANCELED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت لغوشده قابل تغییر نیست");
        }
        if (target == AppointmentStatus.COMPLETED && a.getDate().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "نوبت آینده را نمی‌توان انجام‌شده علامت زد");
        }
        a.setStatus(target);
        appointmentRepository.save(a);

        // اطلاع‌رسانی به بیمار
        Long doctorUserId = a.getDoctor().getUserId();
        if (target == AppointmentStatus.CANCELED) {
            notificationService.notifyUser(a.getPatient().getId(), "لغو نوبت توسط پزشک",
                    "نوبت شما با «" + a.getDoctor().getFullName() + "» در تاریخ "
                            + Jalali.faDate(a.getDate()) + " ساعت " + Jalali.faTime(a.getTime())
                            + " توسط پزشک لغو شد.",
                    "APPOINTMENT_CANCELED");
        } else if (target == AppointmentStatus.CONFIRMED) {
            notificationService.notifyUser(a.getPatient().getId(), "تأیید نوبت",
                    "نوبت شما با «" + a.getDoctor().getFullName() + "» در تاریخ "
                            + Jalali.faDate(a.getDate()) + " ساعت " + Jalali.faTime(a.getTime())
                            + " توسط پزشک تأیید شد.",
                    "APPOINTMENT_CONFIRMED");
        }
        return AppointmentResponse.from(a);
    }

    @Transactional(readOnly = true)
    public DoctorDashboardResponse doctorDashboard(Doctor doctor) {
        DoctorDashboardResponse d = new DoctorDashboardResponse();
        LocalDate today = LocalDate.now();
        List<AppointmentStatus> active = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        d.setTodayCount(appointmentRepository.countByDoctorIdAndDateAndStatusNot(
                doctor.getId(), today, AppointmentStatus.CANCELED));
        d.setUpcomingCount(appointmentRepository.countByDoctorIdAndStatusInAndDateGreaterThanEqual(
                doctor.getId(), active, today));
        d.setCompletedCount(appointmentRepository.countByDoctorIdAndStatus(
                doctor.getId(), AppointmentStatus.COMPLETED));
        d.setCanceledCount(appointmentRepository.countByDoctorIdAndStatus(
                doctor.getId(), AppointmentStatus.CANCELED));
        appointmentRepository.findFirstByDoctorIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
                        doctor.getId(), active, today)
                .ifPresent(a -> d.setNextAppointment(AppointmentResponse.from(a)));
        return d;
    }

    // ---------- انقضای خودکار نوبت‌ها ----------

    /**
     * جمع‌بندی نوبت‌های گذشته (توسط اسکجولر شبانه و هنگام نمایش لیست‌ها):
     * - PENDING که تاریخش گذشته → CANCELED (منقضی، چون تأیید نشده)
     * - CONFIRMED که تاریخش گذشته → COMPLETED (انجام‌شده)
     */
    @Transactional
    public int processExpired() {
        List<Appointment> expired = appointmentRepository.findByStatusInAndDateBefore(
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED), LocalDate.now());
        expireList(expired);
        return expired.size();
    }

    @Transactional
    void expireForUser(Long userId) {
        expireList(appointmentRepository.findByPatientIdAndStatusInAndDateBefore(
                userId, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED), LocalDate.now()));
    }

    @Transactional
    void expireForDoctor(Long doctorId) {
        expireList(appointmentRepository.findByDoctorIdAndStatusInAndDateBefore(
                doctorId, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED), LocalDate.now()));
    }

    private void expireList(List<Appointment> list) {
        for (Appointment a : list) {
            if (a.getStatus() == AppointmentStatus.PENDING) {
                a.setStatus(AppointmentStatus.CANCELED);
                notificationService.notifyUser(a.getPatient().getId(), "انقضای نوبت",
                        "نوبت شما با «" + a.getDoctor().getFullName() + "» در تاریخ "
                                + Jalali.faDate(a.getDate()) + " به دلیل عدم تأیید توسط پزشک منقضی شد.",
                        "APPOINTMENT_EXPIRED");
            } else if (a.getStatus() == AppointmentStatus.CONFIRMED) {
                a.setStatus(AppointmentStatus.COMPLETED);
                notificationService.notifyUser(a.getPatient().getId(), "پایان نوبت",
                        "نوبت شما با «" + a.getDoctor().getFullName() + "» در تاریخ "
                                + Jalali.faDate(a.getDate()) + " ساعت " + Jalali.faTime(a.getTime())
                                + " به پایان رسید.",
                        "APPOINTMENT_COMPLETED");
            }
        }
        appointmentRepository.saveAll(list);
    }

    // ---------- ابزارها ----------

    private void validateSlot(Doctor doctor, LocalDate date, LocalTime time, Appointment self) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "تاریخ انتخابی در گذشته است");
        }
        if (date.isAfter(LocalDate.now().plusDays(30))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "رزرو فقط تا ۳۰ روز آینده ممکن است");
        }
        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ساعت انتخابی گذشته است؛ زمان دیگری انتخاب کنید");
        }
        if (!doctor.availableOn(date)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "پزشک در این روز ویزیت ندارد");
        }
        LocalTime start = LocalTime.of(doctor.getStartHour(), 0);
        LocalTime end = LocalTime.of(doctor.getEndHour(), 0);
        if (time.isBefore(start) || !time.isBefore(end)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ساعت انتخابی در بازه کاری پزشک نیست");
        }
        if (time.getMinute() % doctor.getSlotMinutes() != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ساعت انتخابی با بازه‌های نوبت‌دهی هم‌خوانی ندارد");
        }
        boolean booked = appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(
                doctor.getId(), date, time, AppointmentStatus.CANCELED);
        if (booked) {
            throw new ApiException(HttpStatus.CONFLICT, "این زمان قبلاً رزرو شده است. زمان دیگری انتخاب کنید");
        }
    }

    private Appointment findOwned(User user, Long id) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "نوبت پیدا نشد"));
        if (!a.getPatient().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "شما به این نوبت دسترسی ندارید");
        }
        return a;
    }

    private boolean isPast(Appointment a) {
        return a.getDate().isBefore(LocalDate.now())
                || (a.getDate().equals(LocalDate.now()) && a.getTime().isBefore(LocalTime.now()));
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(ir.artor.badoki.util.FaDigits.toEn(s));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "فرمت تاریخ نامعتبر است");
        }
    }

    private LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(ir.artor.badoki.util.FaDigits.toEn(s));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "فرمت ساعت نامعتبر است");
        }
    }
}
