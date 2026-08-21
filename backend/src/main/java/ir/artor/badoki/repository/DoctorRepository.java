package ir.artor.badoki.repository;

import ir.artor.badoki.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("""
        select d from Doctor d
        where (:q is null or lower(d.fullName) like concat('%', lower(cast(:q as text)), '%'))
          and (:specialty is null or d.specialty = cast(:specialty as text))
          and (:city is null or d.city = cast(:city as text))
        order by d.rating desc, d.id asc
        """)
    Page<Doctor> search(@Param("q") String q,
                        @Param("specialty") String specialty,
                        @Param("city") String city,
                        Pageable pageable);

    @Query("select distinct d.specialty from Doctor d order by d.specialty")
    List<String> findDistinctSpecialties();

    @Query("select distinct d.city from Doctor d order by d.city")
    List<String> findDistinctCities();

    Optional<Doctor> findByUserId(Long userId);

    long countBy();
}
