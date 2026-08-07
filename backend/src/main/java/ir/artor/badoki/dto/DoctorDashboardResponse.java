package ir.artor.badoki.dto;

/** آمار داشبورد پزشک */
public class DoctorDashboardResponse {

    private long todayCount;
    private long upcomingCount;
    private long completedCount;
    private long canceledCount;
    private AppointmentResponse nextAppointment;

    public DoctorDashboardResponse() {}

    public long getTodayCount() { return todayCount; }
    public void setTodayCount(long todayCount) { this.todayCount = todayCount; }

    public long getUpcomingCount() { return upcomingCount; }
    public void setUpcomingCount(long upcomingCount) { this.upcomingCount = upcomingCount; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getCanceledCount() { return canceledCount; }
    public void setCanceledCount(long canceledCount) { this.canceledCount = canceledCount; }

    public AppointmentResponse getNextAppointment() { return nextAppointment; }
    public void setNextAppointment(AppointmentResponse nextAppointment) { this.nextAppointment = nextAppointment; }
}
