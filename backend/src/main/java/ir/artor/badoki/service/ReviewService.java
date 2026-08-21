package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.ReviewRequest;
import ir.artor.badoki.dto.ReviewResponse;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Doctor;
import ir.artor.badoki.model.Review;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.AppointmentRepository;
import ir.artor.badoki.repository.DoctorRepository;
import ir.artor.badoki.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** مدیریت امتیاز و نظر بیماران درباره پزشکان */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    public ReviewService(ReviewRepository reviewRepository,
                         DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         DoctorService doctorService) {
        this.reviewRepository = reviewRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }

    /** فهرست نظرات یک پزشک (جدیدترین اول) */
    @Transactional(readOnly = true)
    public List<ReviewResponse> listForDoctor(Long doctorId) {
        doctorService.findDoctor(doctorId);
        return reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * ثبت نظر — شرایط:
     * ۱) بیمار حداقل یک نوبت «انجام‌شده» (یا قطعیِ گذشته) با این پزشک داشته باشد
     * ۲) قبلاً برای همین پزشک نظر ثبت نکرده باشد (یک نظر برای هر پزشک)
     */
    @Transactional
    public ReviewResponse create(User patient, ReviewRequest req) {
        Doctor doctor = doctorService.findDoctor(req.getDoctorId());

        boolean eligible = appointmentRepository.hasEligibleVisit(
                patient.getId(), doctor.getId(),
                AppointmentStatus.COMPLETED, AppointmentStatus.CONFIRMED, java.time.LocalDate.now());
        if (!eligible) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "برای ثبت نظر باید حداقل یک نوبت انجام‌شده با این پزشک داشته باشید");
        }
        if (reviewRepository.findByPatientIdAndDoctorId(patient.getId(), doctor.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "شما قبلاً برای این پزشک نظر ثبت کرده‌اید");
        }

        Review review = new Review();
        review.setPatient(patient);
        review.setDoctor(doctor);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        reviewRepository.save(review);

        // به‌روزرسانی تدریجی امتیاز و تعداد نظرات پزشک
        int newCount = doctor.getReviewCount() + 1;
        double newRating = (doctor.getRating() * doctor.getReviewCount() + req.getRating()) / newCount;
        doctor.setRating(Math.round(newRating * 10) / 10.0);
        doctor.setReviewCount(newCount);
        doctorRepository.save(doctor);

        return ReviewResponse.from(review);
    }

    /** حذف نظر توسط صاحب آن */
    @Transactional
    public void delete(User patient, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "نظر پیدا نشد"));
        if (!review.getPatient().getId().equals(patient.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "شما به این نظر دسترسی ندارید");
        }
        Doctor doctor = review.getDoctor();
        reviewRepository.delete(review);

        // بازگرداندن تدریجی امتیاز
        int newCount = doctor.getReviewCount() - 1;
        if (newCount <= 0) {
            doctor.setRating(0);
            doctor.setReviewCount(0);
        } else {
            double newRating = (doctor.getRating() * doctor.getReviewCount() - review.getRating()) / newCount;
            doctor.setRating(Math.max(0, Math.round(newRating * 10) / 10.0));
            doctor.setReviewCount(newCount);
        }
        doctorRepository.save(doctor);
    }
}
