package ir.artor.badoki.util;

/** تبدیل ارقام فارسی/عربی به انگلیسی — برای تحمل خطاهای ورودی کلاینت */
public final class FaDigits {

    private FaDigits() {}

    public static String toEn(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
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
}
