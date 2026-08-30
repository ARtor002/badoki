package ir.artor.badoki.util;

import android.content.Context;

import ir.artor.badoki.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** ابزارهای فرمت‌بندی فارسی (اعداد، تومان، تاریخ) */
public class Fmt {

    private static final char[] FA_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    /** تبدیل ارقام فارسی/عربی به انگلیسی — برای ارسال اعداد به سرور */
    public static String en(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '۰' && c <= '۹') {
                sb.append((char) ('0' + (c - '۰')));
            } else if (c >= '٠' && c <= '٩') {
                sb.append((char) ('0' + (c - '٠')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** فقط ارقام لاتین → فارسی؛ نقطه اعشار دست نمی‌خورد */
    public static String faDigits(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(FA_DIGITS[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** تبدیل ارقام لاتین به فارسی */
    public static String fa(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(FA_DIGITS[c - '0']);
            } else if (c == '.') {
                sb.append('٫');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String fa(long n) {
        return fa(String.valueOf(n));
    }

    /** فرمت تومان با جداکننده هزارگان فارسی: ۳۵۰٬۰۰۰ تومان */
    public static String toman(long amount) {
        String s = String.format(Locale.US, "%,d", amount);
        return fa(s.replace(',', '٬')) + " تومان";
    }

    /** فقط عدد با جداکننده: ۳۵۰٬۰۰۰ */
    public static String tomanNumber(long amount) {
        String s = String.format(Locale.US, "%,d", amount);
        return fa(s.replace(',', '٬'));
    }

    public static String faTime(String hhmm) {
        if (hhmm == null || hhmm.isEmpty()) return "";
        return fa(hhmm);
    }

    /** «۱۴۰۵/۰۶/۰۸ ۲۳:۴۵» از ISO instant (createdAt اعلان) */
    public static String dateTimeJalali(String isoInstant) {
        if (isoInstant == null || isoInstant.isEmpty()) return "";
        try {
            LocalDateTime dt = LocalDateTime.ofInstant(
                    Instant.parse(isoInstant), ZoneId.systemDefault());
            return dateNumeric(dt.toLocalDate()) + " " + fa(pad2(dt.getHour()) + ":" + pad2(dt.getMinute()));
        } catch (Exception e) {
            return "";
        }
    }

    /** «۱۴۰۵/۰۶/۰۸» از LocalDate میلادی */
    public static String dateNumeric(LocalDate g) {
        if (g == null) return "";
        Jalali.JDate j = Jalali.toJalali(g);
        return fa(j.jy + "/" + pad2(j.jm) + "/" + pad2(j.jd));
    }

    /** «۱۴۰۵/۰۶/۰۸» از رشته yyyy-MM-dd */
    public static String dateNumeric(String isoDate) {
        LocalDate g = parseIso(isoDate);
        if (g == null) return isoDate == null ? "" : isoDate;
        return dateNumeric(g);
    }

    private static String pad2(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    /** «شنبه ۱۵ مرداد ۱۴۰۵» */
    public static String dateFull(Context ctx, String iso) {
        LocalDate g = parseIso(iso);
        if (g == null) return iso == null ? "" : iso;
        Jalali.JDate j = Jalali.toJalali(g);
        return Jalali.weekdayName(g) + " " + fa(j.jd) + " " + monthName(ctx, j.jm) + " " + fa(j.jy);
    }

    /** «۱۵ مرداد» */
    public static String dateShort(Context ctx, String iso) {
        LocalDate g = parseIso(iso);
        if (g == null) return iso == null ? "" : iso;
        Jalali.JDate j = Jalali.toJalali(g);
        return fa(j.jd) + " " + monthName(ctx, j.jm);
    }

    /** «شنبه ۱۵ مرداد» */
    public static String dateWithWeekday(Context ctx, LocalDate g) {
        Jalali.JDate j = Jalali.toJalali(g);
        return Jalali.weekdayName(g) + " " + fa(j.jd) + " " + monthName(ctx, j.jm);
    }

    public static LocalDate parseIso(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            return LocalDate.parse(iso);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String monthName(Context ctx, int jm) {
        String[] res = ctx.getResources().getStringArray(R.array.jalali_months);
        if (jm < 1 || jm > 12) return "";
        return res[jm - 1];
    }

    public static String todayFull(Context ctx) {
        return dateWithWeekday(ctx, LocalDate.now());
    }

    /** نمره با ممیز فارسی: ۴٫۹ */
    public static String rating(double r) {
        return fa(String.format(Locale.US, "%.1f", r));
    }
}
