package ir.artor.badoki.repository;

import ir.artor.badoki.model.Appointment;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByDateDescTimeDesc(Long patientId);

    List<Appointment> findByPatientIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(Long patientId, LocalDate from);

    List<Appointment> findByPatientIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
            Long patientId, List<AppointmentStatus> statuses, LocalDate from);

    List<Appointment> findByPatientIdAndDateLessThanOrderByDateDescTimeDesc(Long patientId, LocalDate from);

    List<Appointment> findByPatientIdAndStatusOrderByDateDescTimeDesc(Long patientId, AppointmentStatus status);

    Optional<Appointment> findFirstByPatientIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
            Long patientId, List<AppointmentStatus> statuses, LocalDate from);

    long countByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorIdAndDateAndStatusNot(Long doctorId, LocalDate date, AppointmentStatus excluded);

    List<Appointment> findByDoctorIdOrderByDateDescTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
            Long doctorId, List<AppointmentStatus> statuses, LocalDate from);

    @Query("""
            select a from Appointment a
            where a.doctor.id = :doctorId
              and (a.date < :today or a.status = :completed)
            order by a.date desc, a.time desc
            """)
    List<Appointment> findPastForDoctor(@Param("doctorId") Long doctorId,
                                        @Param("today") LocalDate today,
                                        @Param("completed") AppointmentStatus completed);

    long countByDoctorIdAndDateAndStatusNot(Long doctorId, LocalDate date, AppointmentStatus excluded);

    long countByDoctorIdAndStatusInAndDateGreaterThanEqual(
            Long doctorId, List<AppointmentStatus> statuses, LocalDate from);

    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByStatus(AppointmentStatus status);

    /** نوبت‌های گذشته که هنوز بسته نشده‌اند (برای انقضای خودکار) */
    List<Appointment> findByStatusInAndDateBefore(List<AppointmentStatus> statuses, LocalDate date);

    List<Appointment> findByPatientIdAndStatusInAndDateBefore(
            Long patientId, List<AppointmentStatus> statuses, LocalDate date);

    List<Appointment> findByDoctorIdAndStatusInAndDateBefore(
            Long doctorId, List<AppointmentStatus> statuses, LocalDate date);

    Optional<Appointment> findFirstByDoctorIdAndStatusInAndDateGreaterThanEqualOrderByDateAscTimeAsc(
            Long doctorId, List<AppointmentStatus> statuses, LocalDate from);

    boolean existsByDoctorIdAndDateAndTimeAndStatusNot(
            Long doctorId, LocalDate date, LocalTime time, AppointmentStatus excluded);

    boolean existsByPatientIdAndDoctorIdAndDateAndTimeAndStatusNot(
            Long patientId, Long doctorId, LocalDate date, LocalTime time, AppointmentStatus excluded);
}

