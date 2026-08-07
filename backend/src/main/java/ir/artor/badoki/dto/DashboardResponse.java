package ir.artor.badoki.dto;

/** آمار داشبورد کاربر */
public class DashboardResponse {

    private long upcomingCount;
    private long completedCount;
    private long canceledCount;
    private long totalDoctors;
    private AppointmentResponse nextAppointment;

    public DashboardResponse() {}

    public long getUpcomingCount() { return upcomingCount; }
    public void setUpcomingCount(long upcomingCount) { this.upcomingCount = upcomingCount; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getCanceledCount() { return canceledCount; }
    public void setCanceledCount(long canceledCount) { this.canceledCount = canceledCount; }

    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }

    public AppointmentResponse getNextAppointment() { return nextAppointment; }
    public void setNextAppointment(AppointmentResponse nextAppointment) { this.nextAppointment = nextAppointment; }
}
