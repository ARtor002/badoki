package ir.artor.badoki.model;

import jakarta.persistence.*;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "doctors")

public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 80)
    private String specialty;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(length = 120)
    private String hospitalName;

    @Column(length = 300)
    private String address;

    @Column(length = 1000)
    private String bio;

    /** هزینه ویزیت به تومان */
    @Column(nullable = false)
    private long visitPrice;

    @Column(nullable = false)
    private int experienceYears;

    @Column(nullable = false)
    private double rating = 4.5;

    @Column(nullable = false)
    private int reviewCount = 0;

    /** روزهای کاری به صورت CSV از نام‌های DayOfWeek (مثلاً SATURDAY,SUNDAY) */
    @Column(nullable = false, length = 120)
    private String availableDays;

    @Column(nullable = false)
    private int startHour = 9;

    @Column(nullable = false)
    private int endHour = 17;

    @Column(nullable = false)
    private int slotMinutes = 30;

    /** شناسه حساب کاربری (User) که این پروفایل پزشک به آن متصل است — برای نقش DOCTOR */
    @Column(unique = true)
    private Long userId;

    @Column(nullable = false)
    private LocalTime createdAt = LocalTime.now();

    public boolean availableOn(LocalDate date) {
        Set<DayOfWeek> days = new HashSet<>();
        if (availableDays != null) {
            Arrays.stream(availableDays.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(DayOfWeek::valueOf)
                    .forEach(days::add);
        }
        return days.contains(date.getDayOfWeek());
    }

    public List<String> getAvailableDaysList() {
        if (availableDays == null || availableDays.isBlank()) return List.of();
        return Arrays.stream(availableDays.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public void setAvailableDaysList(List<String> days) {
        this.availableDays = String.join(",", days);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public long getVisitPrice() {
        return visitPrice;
    }

    public void setVisitPrice(long visitPrice) {
        this.visitPrice = visitPrice;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getSlotMinutes() {
        return slotMinutes;
    }

    public void setSlotMinutes(int slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalTime createdAt) {
        this.createdAt = createdAt;
    }
}
