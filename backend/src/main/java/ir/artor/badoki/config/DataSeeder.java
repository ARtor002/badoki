package ir.artor.badoki.config;

import ir.artor.badoki.model.Appointment;
import ir.artor.badoki.model.AppointmentStatus;
import ir.artor.badoki.model.Doctor;
import ir.artor.badoki.model.Role;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.AppointmentRepository;
import ir.artor.badoki.repository.DoctorRepository;
import ir.artor.badoki.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** داده‌های نمونه واقع‌گرایانه برای اولین اجرا */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      DoctorRepository doctorRepository,
                      AppointmentRepository appointmentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final List<DayOfWeek> WEEKDAYS = List.of(
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("داده‌ها از قبل وجود دارند — از سید کردن صرف‌نظر شد");
            return;
        }
        log.info("در حال بارگذاری داده‌های نمونه...");

        // ---------- کاربران ----------
        User admin = user("مدیر سیستم", "admin@badoki.ir", "09120000000", "admin123", Role.ADMIN);
        User demo = user("علی محمدی", "ali@example.com", "09121234567", "123456", Role.PATIENT);
        User maryam = user("مریم نعمتی", "maryam@example.com", "09351112233", "123456", Role.PATIENT);
        User reza = user("رضا کاظمی", "reza@example.com", "09194445566", "123456", Role.PATIENT);
        User negar = user("نگار صادقی", "negar@example.com", "09217778899", "123456", Role.PATIENT);
        userRepository.saveAll(List.of(admin, demo, maryam, reza, negar));

        // ---------- پزشکان ----------
        List<Doctor> doctors = List.of(
                doctor("دکتر علی رضایی", "قلب و عروق", "تهران", "بیمارستان میلاد",
                        "خیابان صادقیه، بیمارستان میلاد، طبقه ۵",
                        "متخصص قلب و عروق و فلوشیپ اینترونشنال کاردیولوژی. عضو انجمن قلب ایران و اروپا.",
                        350_000, 18, 4.9, 312, 9, 17),
                doctor("دکتر مریم احمدی", "پوست و مو", "تهران", "کلینیک تخصصی پارس",
                        "سعادت‌آباد، میدان کاج، کلینیک پارس، واحد ۱۲",
                        "متخصص پوست، مو و زیبایی. انجام لیزر و درمان تخصصی ریزش مو با جدیدترین متدها.",
                        400_000, 12, 4.8, 248, 10, 20),
                doctor("دکتر حسین کریمی", "ارتوپدی", "اصفهان", "بیمارستان الزهرا",
                        "اصفهان، خیابان چهارباغ بالا، بیمارستان الزهرا",
                        "جراح ارتوپد با بیش از ۲۰ سال سابقه. تخصص در جراحی تعویض مفصل زانو و لگن.",
                        300_000, 22, 4.7, 189, 9, 17),
                doctor("دکتر فاطمه محمدی", "کودکان", "تهران", "بیمارستان کودکان مفید",
                        "تهران، خیابان شهید بهشتی، بیمارستان مفید",
                        "متخصص بیماری‌های کودکان و نوزادان. عضو هیئت علمی دانشگاه علوم پزشکی شهید بهشتی.",
                        250_000, 10, 4.9, 421, 9, 17),
                doctor("دکتر رضا نادری", "مغز و اعصاب", "مشهد", "بیمارستان قائم",
                        "مشهد، خیابان ابن‌سینا، بیمارستان قائم",
                        "متخصص مغز و اعصاب. درمان سردردهای مزمن، ام‌اس و بیماری‌های حرکتی.",
                        380_000, 16, 4.6, 154, 9, 17),
                doctor("دکتر زهرا حسینی", "زنان و زایمان", "تهران", "بیمارستان امام خمینی",
                        "تهران، خیابان دکتر قریب، بیمارستان امام خمینی",
                        "متخصص زنان، زایمان و نازایی. عضو انجمن مامایی و نازایی ایران.",
                        320_000, 14, 4.8, 276, 9, 17),
                doctor("دکتر محمد قاسمی", "داخلی", "شیراز", "بیمارستان نمازی",
                        "شیراز، خیابان ملاصدرا، بیمارستان نمازی",
                        "متخصص بیماری‌های داخلی. تشخیص و درمان دیابت، فشار خون و بیماری‌های گوارشی.",
                        220_000, 20, 4.5, 198, 9, 17),
                doctor("دکتر نسرین موسوی", "چشم پزشکی", "تهران", "بیمارستان فارابی",
                        "تهران، خیابان قزوین، بیمارستان فارابی",
                        "متخصص چشم و جراح عیوب انکساری. انجام لیزر چشم (PRK و لازک) و جراحی آب مروارید.",
                        350_000, 11, 4.7, 143, 9, 17),
                doctor("دکتر امیر تهرانی", "گوش، حلق و بینی", "کرج", "بیمارستان امام علی",
                        "کرج، میدان شهدا، بیمارستان امام علی",
                        "متخصص گوش، حلق و بینی و جراح سر و گردن. درمان تخصصی اختلالات شنوایی و سینوزیت.",
                        280_000, 13, 4.4, 97, 9, 17),
                doctor("دکتر سارا رحیمی", "روانپزشکی", "تهران", "کلینیک آتیه",
                        "تهران، شهرک غرب، خیابان ایران‌زمین، کلینیک آتیه",
                        "روانپزشک و روان‌درمانگر. درمان اضطراب، افسردگی و اختلالات خواب.",
                        450_000, 9, 4.9, 265, 10, 20),
                doctor("دکتر بهرام شریفی", "اورولوژی", "اصفهان", "بیمارستان عیسی بن مریم",
                        "اصفهان، خیابان نظر شرقی، بیمارستان عیسی بن مریم",
                        "متخصص اورولوژی و جراح کلیه و مجاری ادراری.",
                        340_000, 19, 4.6, 121, 9, 17),
                doctor("دکتر لیلا کاظمی", "دندانپزشکی", "تهران", "کلینیک دندانپزشکی لبخند",
                        "تهران، خیابان ولیعصر، بالاتر از پارک ساعی، کلینیک لبخند",
                        "دندانپزشک ترمیمی و زیبایی. ایمپلنت، لمینت و درمان ریشه با تجهیزات مدرن.",
                        500_000, 8, 4.8, 233, 9, 19),
                doctor("دکتر کامران یوسفی", "قلب و عروق", "تبریز", "بیمارستان شهید مدنی",
                        "تبریز، خیابان آزادی، بیمارستان شهید مدنی",
                        "متخصص قلب و عروق و آنژیوگرافی. عضو انجمن قلب آذربایجان شرقی.",
                        300_000, 15, 4.5, 108, 9, 17),
                doctor("دکتر الهام صادقی", "پوست و مو", "شیراز", "کلینیک زیبایی سعدی",
                        "شیراز، خیابان زند، کلینیک زیبایی سعدی",
                        "متخصص پوست و مو. جوان‌سازی پوست، مزوتراپی و درمان اسکار آکنه.",
                        380_000, 7, 4.7, 176, 10, 19),
                doctor("دکتر مهدی عباسی", "ارتوپدی", "تهران", "بیمارستان شریعتی",
                        "تهران، خیابان کارگر شمالی، بیمارستان شریعتی",
                        "جراح ارتوپد. تخصص در جراحی ستون فقرات و درمان دیسک کمر.",
                        360_000, 17, 4.6, 210, 9, 17),
                doctor("دکتر پریسا نوری", "کودکان", "مشهد", "بیمارستان دکتر شیخ",
                        "مشهد، خیابان دانشگاه، بیمارستان دکتر شیخ",
                        "متخصص بیماری‌های کودکان. واکسیناسیون، رشد و تغذیه کودک.",
                        240_000, 12, 4.8, 167, 9, 17),
                doctor("دکتر فرهاد جعفری", "داخلی", "تهران", "بیمارستان بهارلو",
                        "تهران، خیابان کارگر جنوبی، بیمارستان بهارلو",
                        "فوق تخصص گوارش و کبد. انجام آندوسکوپی و کولونوسکوپی.",
                        210_000, 25, 4.5, 289, 9, 17),
                doctor("دکتر شیما اکبری", "مغز و اعصاب", "اصفهان", "بیمارستان آیت‌الله کاشانی",
                        "اصفهان، خیابان کاشانی، بیمارستان کاشانی",
                        "متخصص مغز و اعصاب. درمان صرع، پارکینسون و بیماری‌های عروقی مغز.",
                        350_000, 10, 4.7, 132, 9, 17)
        );
        doctorRepository.saveAll(doctors);

        // ---------- حساب‌های پزشک (نقش DOCTOR) ----------
        User drAli = user("دکتر علی رضایی", "doctor@example.com", "09121234001", "123456", Role.DOCTOR);
        User drMaryam = user("دکتر مریم احمدی", "dr.maryam@example.com", "09121234002", "123456", Role.DOCTOR);
        User drHossein = user("دکتر حسین کریمی", "dr.reza@example.com", "09121234003", "123456", Role.DOCTOR);
        User drFatemeh = user("دکتر فاطمه محمدی", "dr.fatemeh@example.com", "09121234004", "123456", Role.DOCTOR);
        userRepository.saveAll(List.of(drAli, drMaryam, drHossein, drFatemeh));

        doctors.get(0).setUserId(drAli.getId());
        doctors.get(1).setUserId(drMaryam.getId());
        doctors.get(2).setUserId(drHossein.getId());
        doctors.get(3).setUserId(drFatemeh.getId());
        doctorRepository.saveAll(doctors);

        // ---------- نوبت‌ها ----------
        LocalDate today = LocalDate.now();

        // نوبت‌های علی محمدی (بیمار دمو)
        appointment(demo, doctors.get(0), today.plusDays(2), LocalTime.of(10, 0),
                AppointmentStatus.CONFIRMED, "درد خفیف قفسه سینه هنگام فعالیت");
        appointment(demo, doctors.get(2), today.plusDays(1), LocalTime.of(9, 30),
                AppointmentStatus.PENDING, "درد زانوی راست پس از پیاده‌روی");
        appointment(demo, doctors.get(3), today.plusDays(5), LocalTime.of(11, 30),
                AppointmentStatus.PENDING, "معاینه دوره‌ای کودک");
        appointment(demo, doctors.get(6), today.minusDays(25), LocalTime.of(9, 0),
                AppointmentStatus.COMPLETED, null);
        appointment(demo, doctors.get(8), today.plusDays(1), LocalTime.of(17, 0),
                AppointmentStatus.CANCELED, null);

        // نوبت‌های سایر بیماران تا لیست ساعات کمی «پر» به نظر برسد
        appointment(maryam, doctors.get(0), today.plusDays(2), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, null);
        appointment(maryam, doctors.get(1), today.plusDays(3), LocalTime.of(12, 0),
                AppointmentStatus.PENDING, "بررسی لکه پوستی");
        appointment(reza, doctors.get(2), today.plusDays(2), LocalTime.of(11, 0),
                AppointmentStatus.CONFIRMED, "درد زانو");
        appointment(reza, doctors.get(4), today.minusDays(10), LocalTime.of(16, 30),
                AppointmentStatus.COMPLETED, null);
        appointment(negar, doctors.get(5), today.plusDays(4), LocalTime.of(9, 30),
                AppointmentStatus.PENDING, null);

        log.info("داده‌های نمونه بارگذاری شد: {} کاربر، {} پزشک، {} نوبت",
                userRepository.count(), doctorRepository.count(), appointmentRepository.count());
    }

    private User user(String name, String email, String phone, String password, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        return u;
    }

    private Doctor doctor(String name, String specialty, String city, String hospital,
                          String address, String bio, long price, int experience,
                          double rating, int reviews, int startHour, int endHour) {
        Doctor d = new Doctor();
        d.setFullName(name);
        d.setSpecialty(specialty);
        d.setCity(city);
        d.setHospitalName(hospital);
        d.setAddress(address);
        d.setBio(bio);
        d.setVisitPrice(price);
        d.setExperienceYears(experience);
        d.setRating(rating);
        d.setReviewCount(reviews);
        d.setAvailableDaysList(WEEKDAYS.stream().map(Enum::name).toList());
        d.setStartHour(startHour);
        d.setEndHour(endHour);
        d.setSlotMinutes(30);
        return d;
    }

    private void appointment(User patient, Doctor doctor, LocalDate date, LocalTime time,
                             AppointmentStatus status, String notes) {
        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setDate(date);
        a.setTime(time);
        a.setStatus(status);
        a.setNotes(notes);
        appointmentRepository.save(a);
    }
}
