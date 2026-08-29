package ir.artor.badoki.repository;

import ir.artor.badoki.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DoctorRepositorySearchTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @BeforeEach
    void seed() {
        doctorRepository.save(doctor("دکتر علی رضایی", "قلب و عروق", "تهران", "بیمارستان میلاد"));
        doctorRepository.save(doctor("دکتر مریم احمدی", "پوست و مو", "تهران", "کلینیک تخصصی پارس"));
        doctorRepository.save(doctor("دکتر حسین کریمی", "ارتوپدی", "اصفهان", "بیمارستان الزهرا"));
        doctorRepository.save(doctor("دکتر فاطمه محمدی", "کودکان", "تهران", "بیمارستان کودکان مفید"));
    }

    @Test
    void filtersCombineWithAndLogic() {
        Page<Doctor> tehran = doctorRepository.search(null, null, "تهران", null, PageRequest.of(0, 20));
        assertThat(tehran.getTotalElements()).isEqualTo(3);

        Page<Doctor> heartTehran = doctorRepository.search(
                null, "قلب و عروق", "تهران", null, PageRequest.of(0, 20));
        assertThat(heartTehran.getTotalElements()).isEqualTo(1);
        assertThat(heartTehran.getContent().get(0).getFullName()).contains("علی رضایی");

        Page<Doctor> milad = doctorRepository.search(
                null, null, null, "بیمارستان میلاد", PageRequest.of(0, 20));
        assertThat(milad.getTotalElements()).isEqualTo(1);
        assertThat(milad.getContent().get(0).getHospitalName()).isEqualTo("بیمارستان میلاد");
    }

    @Test
    void freeTextMatchesNameSpecialtyCityAndHospital() {
        assertThat(doctorRepository.search("علی", null, null, null, PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
        assertThat(doctorRepository.search("قلب", null, null, null, PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
        assertThat(doctorRepository.search("اصفهان", null, null, null, PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
        assertThat(doctorRepository.search("میلاد", null, null, null, PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void distinctMetaLists() {
        assertThat(doctorRepository.findDistinctCities()).containsExactly("اصفهان", "تهران");
        assertThat(doctorRepository.findDistinctSpecialties())
                .contains("ارتوپدی", "پوست و مو", "قلب و عروق", "کودکان");
        assertThat(doctorRepository.findDistinctHospitals())
                .contains("بیمارستان میلاد", "بیمارستان الزهرا", "کلینیک تخصصی پارس");
    }

    private static Doctor doctor(String name, String specialty, String city, String hospital) {
        Doctor d = new Doctor();
        d.setFullName(name);
        d.setSpecialty(specialty);
        d.setCity(city);
        d.setHospitalName(hospital);
        d.setVisitPrice(250_000);
        d.setExperienceYears(10);
        d.setAvailableDays("SATURDAY,SUNDAY,MONDAY");
        d.setStartHour(9);
        d.setEndHour(17);
        d.setSlotMinutes(30);
        return d;
    }
}
