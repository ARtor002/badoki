package ir.artor.badoki.repository;

import ir.artor.badoki.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    Optional<Review> findByPatientIdAndDoctorId(Long patientId, Long doctorId);
}
