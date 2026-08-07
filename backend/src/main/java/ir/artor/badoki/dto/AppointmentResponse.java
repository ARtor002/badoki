package ir.artor.badoki.dto;

import ir.artor.badoki.model.Appointment;

public class AppointmentResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private String specialty;
    private String city;
    private String hospitalName;
    private long visitPrice;
    private String date;
    private String time;
    private String status;
    private String notes;
    private String createdAt;

    /** اطلاعات بیمار — فقط در نمای پزشک پر می‌شود */
    private String patientName;
    private String patientPhone;

    public AppointmentResponse() {}

    public static AppointmentResponse from(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.id = a.getId();
        r.doctorId = a.getDoctor().getId();
        r.doctorName = a.getDoctor().getFullName();
        r.specialty = a.getDoctor().getSpecialty();
        r.city = a.getDoctor().getCity();
        r.hospitalName = a.getDoctor().getHospitalName();
        r.visitPrice = a.getDoctor().getVisitPrice();
        r.date = a.getDate().toString();
        r.time = a.getTime().toString();
        r.status = a.getStatus().name();
        r.notes = a.getNotes();
        r.createdAt = a.getCreatedAt() != null ? a.getCreatedAt().toString() : null;
        r.patientName = a.getPatient() != null ? a.getPatient().getFullName() : null;
        r.patientPhone = a.getPatient() != null ? a.getPatient().getPhone() : null;
        return r;
    }

    public Long getId() { return id; }
    public Long getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialty() { return specialty; }
    public String getCity() { return city; }
    public String getHospitalName() { return hospitalName; }
    public long getVisitPrice() { return visitPrice; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }

    public String getPatientName() { return patientName; }
    public String getPatientPhone() { return patientPhone; }
}
