package ir.artor.badoki.config;

import ir.artor.badoki.service.AppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** وظایف زمان‌بندی‌شده سامانه */
@Component
public class AppointmentScheduler {

    private final AppointmentService appointmentService;

    public AppointmentScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** هر شب ساعت ۰۰:۱۵ — نوبت‌های گذشته را جمع‌بندی می‌کند:
     *  PENDING گذشته → لغو (منقضی)  و  CONFIRMED گذشته → انجام‌شده  */
    @Scheduled(cron = "${app.scheduler.expire-cron:0 15 0 * * *}")
    public void expireDaily() {
        appointmentService.processExpired();
    }
}
