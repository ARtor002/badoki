# وضعیت پروژه بدوکی

آخرین به‌روزرسانی: ۲۰۲۶-۰۸-۳۰

اگر چت قطع شد، همین فایل را به چت بعدی بده.

---

## تاریخ شمسی در اعلان‌ها

دو منبع میلادی بودند:

1. برچسب زمان در `NotificationAdapter` (`yyyy/MM/dd`) → الان `Fmt.dateTimeJalali`
2. متن پیام در `AppointmentService` با `a.getDate()` (مثل `2026-08-30`) → الان `Jalali.format` / `formatSlot`

اعلان‌های قدیمی هم در کلاینت با regex روی `yyyy-MM-dd` شمسی می‌شوند.

---

## گزارش کامل

فایل ورد برای دانلود (هر دو یکی هستند):

- `report/Badoki-Report.docx`  ← نام انگلیسی؛ در فایل‌ویور باز می‌شود
- `report/گزارش-بدوکی.docx`
- `Badoki-Report.docx` (ریشه مخزن)
- `گزارش-بدوکی.docx` (ریشه مخزن)

اسکریپت‌ها:

- `report/make_diagrams.py` — matplotlib با چیدمان گزارش آرمین. `fa()` در matplotlib همان متن خام است.
- `report/make_plantuml.py` — منابع PlantUML با reshape+bidi.
- `report/make_report.py` — شش فصل؛ B Nazanin ۱۳، فاصله ۱٫۳۵، حاشیه ۲٫۵ از هر چهار طرف.

بازتولید:

```
.venv-report/bin/python report/make_diagrams.py
.venv-report/bin/python report/make_plantuml.py
.venv-report/bin/python report/make_report.py
```

نام دانشجو در جلد: آرمین ترکمندی

---

## پروژه نرم‌افزاری

- بک‌اند Spring Boot + PostgreSQL، اندروید Java، پکیج `ir.artor.badoki`
- نقش‌ها: PATIENT / DOCTOR / ADMIN
- جستجوی پزشک: تخصص + شهر + بیمارستان (AND)
- OTP ایمیل، انقضای نوبت، اطلاع‌رسانی، نظر، آموزش

حساب نمونه: ali@example.com / 123456 — doctor@example.com / 123456 — admin@badoki.ir / admin123

شاخه: `arena/01a04ee3-badoki`
