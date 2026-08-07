package ir.artor.badoki.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** تعریف endpointهای REST سرور دکترسلام */
public interface ApiService {

    // ---------- احراز هویت ----------
    @POST("api/auth/login")
    Call<Models.AuthResponse> login(@Body Models.LoginRequest request);

    @POST("api/auth/register")
    Call<Models.AuthResponse> register(@Body Models.RegisterRequest request);

    // ---------- پروفایل ----------
    @GET("api/me")
    Call<Models.User> me();

    @PUT("api/me")
    Call<Models.User> updateMe(@Body Models.ProfileUpdateRequest request);

    @GET("api/dashboard")
    Call<Models.Dashboard> dashboard();

    // ---------- پزشکان ----------
    @GET("api/doctors")
    Call<Models.DoctorsPage> doctors(@Query("query") String query,
                                     @Query("specialty") String specialty,
                                     @Query("city") String city,
                                     @Query("page") int page,
                                     @Query("size") int size);

    @GET("api/doctors/{id}")
    Call<Models.Doctor> doctor(@Path("id") long id);

    @GET("api/doctors/{id}/slots")
    Call<List<Models.Slot>> slots(@Path("id") long id, @Query("date") String date);

    @GET("api/meta/specialties")
    Call<List<String>> specialties();

    @GET("api/meta/cities")
    Call<List<String>> cities();

    // ---------- نوبت‌ها ----------
    @GET("api/appointments")
    Call<List<Models.Appointment>> appointments(@Query("filter") String filter);

    @POST("api/appointments")
    Call<Models.Appointment> createAppointment(@Body Models.AppointmentRequest request);

    @PUT("api/appointments/{id}")
    Call<Models.Appointment> reschedule(@Path("id") long id, @Body Models.RescheduleRequest request);

    @PUT("api/appointments/{id}/cancel")
    Call<Models.Appointment> cancelAppointment(@Path("id") long id);

    @DELETE("api/appointments/{id}")
    Call<Void> deleteAppointment(@Path("id") long id);

    // ---------- پورتال پزشک (نقش DOCTOR) ----------
    @GET("api/doctor/me")
    Call<Models.Doctor> doctorMe();

    @PUT("api/doctor/me")
    Call<Models.Doctor> updateDoctorMe(@Body Models.DoctorProfileUpdate request);

    @GET("api/doctor/dashboard")
    Call<Models.DoctorDashboard> doctorDashboard();

    @GET("api/doctor/appointments")
    Call<List<Models.Appointment>> doctorAppointments(@Query("filter") String filter);

    @PUT("api/doctor/appointments/{id}/status")
    Call<Models.Appointment> updateAppointmentStatus(@Path("id") long id,
                                                     @Body Models.UpdateStatusRequest request);

    // ---------- پنل ادمین (نقش ADMIN) ----------
    @GET("api/admin/users")
    Call<List<Models.AdminUser>> adminUsers();

    @GET("api/admin/stats")
    Call<Models.AdminStats> adminStats();

    @GET("api/admin/doctors")
    Call<List<Models.Doctor>> adminDoctors();

    @POST("api/admin/doctors")
    Call<Models.Doctor> adminCreateDoctor(@Body Models.AdminDoctorRequest request);

    @PUT("api/admin/doctors/{id}")
    Call<Models.Doctor> adminUpdateDoctor(@Path("id") long id, @Body Models.AdminDoctorRequest request);

    @DELETE("api/admin/doctors/{id}")
    Call<Void> adminDeleteDoctor(@Path("id") long id);
}
