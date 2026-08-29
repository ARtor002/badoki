# وضعیت پروژه بدوکی

آخرین به‌روزرسانی: ۲۰۲۶-۰۸-۳۰

اگر چت قطع شد، همین فایل را به چت بعدی بده.

---

## گزارش کامل (همین مرحله)

فایل ورد برای دانلود (هر دو یکی هستند):

- `report/Badoki-Report.docx`  ← نام انگلیسی؛ در فایل‌ویور باز می‌شود
- `report/گزارش-بدوکی.docx`
- `Badoki-Report.docx` (ریشه مخزن)
- `گزارش-بدوکی.docx` (ریشه مخزن)

اسکریپت‌ها:

- `report/make_diagrams.py` — matplotlib با چیدمان گزارش آرمین (۳ ستون مواردکاربرد، ۳ خط‌عمر توالی، لوزی فعالیت، کلاس+enum). `fa()` در matplotlib همان متن خام است چون reshape متن را خراب می‌کند.
- `report/make_plantuml.py` — منابع PlantUML با reshape+bidi (برای رندر خارجی).
- `report/make_report.py` — شش فصل؛ B Nazanin ۱۳، فاصله ۱٫۳۵، حاشیه ۲٫۵ از هر چهار طرف، شرح جدول و شکل پایین.

بازتولید:

```
.venv-report/bin/python report/make_diagrams.py
.venv-report/bin/python report/make_plantuml.py
.venv-report/bin/python report/make_report.py
```

نام دانشجو در جلد: آرمین ترکمندی — شماره دانشجویی، استاد و دانشگاه را در صفحه عنوان پر کنید.

Java/PlantUML JAR در این محیط نیست؛ PNGها از matplotlib ساخته می‌شوند.

---

## پروژه نرم‌افزاری

- بک‌اند Spring Boot + PostgreSQL، اندروید Java، پکیج `ir.artor.badoki`
- نقش‌ها: PATIENT / DOCTOR / ADMIN
<<<<<<< HEAD
- جستجوی پزشک: تخصص + شهر + بیمارستان (AND) — باگ `metaLoaded` درست شده
=======
- جستجوی پزشک: تخصص + شهر + بیمارستان (AND)
>>>>>>> e027581 (docs: بازسازی دیاگرام‌ها با چیدمان گزارش آرمین و فایل ورد قابل‌دانلود)
- OTP ایمیل، انقضای نوبت، اطلاع‌رسانی، نظر، آموزش

حساب نمونه: ali@example.com / 123456 — doctor@example.com / 123456 — admin@badoki.ir / admin123

شاخه: `arena/01a04ee3-badoki`
