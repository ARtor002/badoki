package ir.artor.badoki.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * تبدیل تاریخ میلادی به شمسی (جلالی) — الگوریتم jalaali-js
 */
public class Jalali {

    private static final int[] BREAKS = {
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    };

    public static class JDate {
        public int jy, jm, jd;
        public JDate(int jy, int jm, int jd) {
            this.jy = jy;
            this.jm = jm;
            this.jd = jd;
        }
    }

    /** تقسیم صحیح با برش به سمت صفر (مطابق ~~ در جاوااسکریپت) */
    private static int div(int a, int b) {
        return a / b;
    }

    private static int mod(int a, int b) {
        return a - (a / b) * b;
    }

    private static int g2d(int gy, int gm, int gd) {
        int d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4)
                + div(153 * mod(gm + 9, 12) + 2, 5)
                + gd - 34840408;
        d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752;
        return d;
    }

    private static int[] d2g(int jdn) {
        int j = 4 * jdn + 139361631;
        j = j + div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908;
        int i = div(mod(j, 1461), 4) * 5 + 308;
        int gd = div(mod(i, 153), 5) + 1;
        int gm = mod(div(i, 153), 12) + 1;
        int gy = div(j, 1461) - 100100 + div(8 - gm, 6);
        return new int[]{gy, gm, gd};
    }

    private static int[] jalCal(int jy) {
        int bl = BREAKS.length;
        int gy = jy + 621;
        int leapJ = -14;
        int jp = BREAKS[0];
        int jump = 0;
        for (int i = 1; i < bl; i++) {
            int jm = BREAKS[i];
            jump = jm - jp;
            if (jy < jm) break;
            leapJ = leapJ + div(jump, 33) * 8 + div(mod(jump, 33), 4);
            jp = jm;
        }
        int n = jy - jp;
        leapJ = leapJ + div(n, 33) * 8 + div(mod(n, 33) + 3, 4);
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1;
        int leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150;
        int march = 20 + leapJ - leapG;
        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33;
        int leap = mod(mod(n + 1, 33) - 1, 4);
        if (leap == -1) leap = 4;
        return new int[]{leap, gy, march};
    }

    private static int j2d(int jy, int jm, int jd) {
        int[] r = jalCal(jy);
        return g2d(r[1], 3, r[2]) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1;
    }

    private static JDate d2j(int jdn) {
        int[] g = d2g(jdn);
        int gy = g[0];
        int jy = gy - 621;
        int[] r = jalCal(jy);
        int jdn1f = g2d(gy, 3, r[2]);
        int jd, jm, k;
        k = jdn - jdn1f;
        if (k >= 0) {
            if (k <= 185) {
                jm = 1 + div(k, 31);
                jd = mod(k, 31) + 1;
                return new JDate(jy, jm, jd);
            } else {
                k -= 186;
            }
        } else {
            jy -= 1;
            k += 179;
            if (r[0] == 1) k += 1;
        }
        jm = 7 + div(k, 30);
        jd = mod(k, 30) + 1;
        return new JDate(jy, jm, jd);
    }

    /** تبدیل تاریخ میلادی به شمسی */
    public static JDate toJalali(LocalDate g) {
        return d2j(g2d(g.getYear(), g.getMonthValue(), g.getDayOfMonth()));
    }

    /** نام روز هفته به فارسی */
    public static String weekdayName(DayOfWeek dow) {
        switch (dow) {
            case SATURDAY: return "شنبه";
            case SUNDAY: return "یکشنبه";
            case MONDAY: return "دوشنبه";
            case TUESDAY: return "سه‌شنبه";
            case WEDNESDAY: return "چهارشنبه";
            case THURSDAY: return "پنجشنبه";
            default: return "جمعه";
        }
    }

    /** نام روز هفته از روی تاریخ میلادی */
    public static String weekdayName(LocalDate g) {
        return weekdayName(g.getDayOfWeek());
    }
}
