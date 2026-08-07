package ir.artor.badoki.dto;

/** آمار پنل ادمین */
public class AdminStatsResponse {

    private long doctors;
    private long patients;
    private long appointments;
    private long pendingAppointments;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long doctors, long patients, long appointments, long pendingAppointments) {
        this.doctors = doctors;
        this.patients = patients;
        this.appointments = appointments;
        this.pendingAppointments = pendingAppointments;
    }

    public long getDoctors() { return doctors; }
    public long getPatients() { return patients; }
    public long getAppointments() { return appointments; }
    public long getPendingAppointments() { return pendingAppointments; }
}
