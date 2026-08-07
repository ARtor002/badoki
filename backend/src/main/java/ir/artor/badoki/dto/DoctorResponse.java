package ir.artor.badoki.dto;

import ir.artor.badoki.model.Doctor;

import java.util.List;

public class DoctorResponse {

    private Long id;
    private String fullName;
    private String specialty;
    private String city;
    private String hospitalName;
    private String address;
    private String bio;
    private long visitPrice;
    private int experienceYears;
    private double rating;
    private int reviewCount;
    private List<String> availableDays;
    private int startHour;
    private int endHour;
    private int slotMinutes;

    public DoctorResponse() {}

    public static DoctorResponse from(Doctor d) {
        DoctorResponse r = new DoctorResponse();
        r.id = d.getId();
        r.fullName = d.getFullName();
        r.specialty = d.getSpecialty();
        r.city = d.getCity();
        r.hospitalName = d.getHospitalName();
        r.address = d.getAddress();
        r.bio = d.getBio();
        r.visitPrice = d.getVisitPrice();
        r.experienceYears = d.getExperienceYears();
        r.rating = d.getRating();
        r.reviewCount = d.getReviewCount();
        r.availableDays = d.getAvailableDaysList();
        r.startHour = d.getStartHour();
        r.endHour = d.getEndHour();
        r.slotMinutes = d.getSlotMinutes();
        return r;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getSpecialty() { return specialty; }
    public String getCity() { return city; }
    public String getHospitalName() { return hospitalName; }
    public String getAddress() { return address; }
    public String getBio() { return bio; }
    public long getVisitPrice() { return visitPrice; }
    public int getExperienceYears() { return experienceYears; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public List<String> getAvailableDays() { return availableDays; }
    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }
    public int getSlotMinutes() { return slotMinutes; }
}
