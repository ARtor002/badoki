package ir.artor.badoki.api;

import java.io.Serializable;
import java.util.List;

/** مدل‌های JSON — نام فیلدها دقیقاً مطابق پاسخ سرور Spring Boot است */
public class Models {

    public static class User implements Serializable {
        public long id;
        public String fullName;
        public String email;
        public String phone;
        public String role;
    }

    public static class AuthResponse implements Serializable {
        public String token;
        public User user;
        public boolean requiresOtp;
        public String email;
        public String message;
        public String devOtp;
    }

    public static class OtpResponse implements Serializable {
        public String message;
        public String devOtp;
    }

    public static class EmailRequest implements Serializable {
        public String email;
    }

    public static class VerifyRegisterRequest implements Serializable {
        public String email;
        public String otp;
        public String fullName;
        public String phone;
        public String password;
    }

    public static class VerifyLoginRequest implements Serializable {
        public String email;
        public String otp;
    }

    public static class ForgotPasswordRequest implements Serializable {
        public String email;
    }

    public static class ResetPasswordRequest implements Serializable {
        public String email;
        public String otp;
        public String newPassword;
    }

    public static class Notification implements Serializable {
        public long id;
        public String title;
        public String message;
        public String type;
        public boolean read;
        public String createdAt;
    }

    public static class UnreadCount implements Serializable {
        public long count;
    }

    public static class RegisterRequest implements Serializable {
        public String fullName;
        public String email;
        public String phone;
        public String password;
    }

    public static class LoginRequest implements Serializable {
        public String email;
        public String password;
    }

    public static class ProfileUpdateRequest implements Serializable {
        public String fullName;
        public String phone;
    }

    public static class Doctor implements Serializable {
        public long id;
        public String fullName;
        public String specialty;
        public String city;
        public String hospitalName;
        public String address;
        public String bio;
        public long visitPrice;
        public int experienceYears;
        public double rating;
        public int reviewCount;
        public List<String> availableDays;
        public int startHour;
        public int endHour;
        public int slotMinutes;
    }

    public static class DoctorsPage implements Serializable {
        public List<Doctor> items;
        public long total;
        public int page;
        public int size;
        public int totalPages;
    }

    public static class Slot implements Serializable {
        public String time;
        public boolean available;
    }

    public static class Appointment implements Serializable {
        public long id;
        public String patientName;
        public String patientPhone;
        public long doctorId;
        public String doctorName;
        public String specialty;
        public String city;
        public String hospitalName;
        public long visitPrice;
        public String date;
        public String time;
        public String status;
        public String notes;
    }

    public static class AppointmentRequest implements Serializable {
        public long doctorId;
        public String date;
        public String time;
        public String notes;
    }

    public static class RescheduleRequest implements Serializable {
        public String date;
        public String time;
    }

    public static class Dashboard implements Serializable {
        public long upcomingCount;
        public long completedCount;
        public long canceledCount;
        public long totalDoctors;
        public Appointment nextAppointment;
    }

    public static class ApiError implements Serializable {
        public int status;
        public String message;
    }
    public static class UpdateStatusRequest implements Serializable {
        public String status;
        public UpdateStatusRequest() {}
        public UpdateStatusRequest(String status) { this.status = status; }
    }

    public static class DoctorProfileUpdate implements Serializable {
        public String hospitalName;
        public String address;
        public String bio;
        public Long visitPrice;
        public Integer startHour;
        public Integer endHour;
        public Integer slotMinutes;
        public java.util.List<String> availableDays;
    }

    public static class AdminDoctorRequest implements Serializable {
        public String fullName;
        public String specialty;
        public String city;
        public String hospitalName;
        public String address;
        public String bio;
        public Long visitPrice;
        public Integer experienceYears;
        public java.util.List<String> availableDays;
        public Integer startHour;
        public Integer endHour;
        public Integer slotMinutes;
        public String email;
        public String password;
    }

    public static class AdminUser implements Serializable {
        public long id;
        public String fullName;
        public String email;
        public String phone;
        public String role;
    }

    public static class AdminStats implements Serializable {
        public long doctors;
        public long patients;
        public long appointments;
        public long pendingAppointments;
    }

    public static class DoctorDashboard implements Serializable {
        public long todayCount;
        public long upcomingCount;
        public long completedCount;
        public long canceledCount;
        public Appointment nextAppointment;
    }
}
