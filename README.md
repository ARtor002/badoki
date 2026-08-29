# 🩺 بدوکی (Badoki) — سامانه رزرو نوبت پزشک

اپلیکیشن **فولاستک** رزرو آنلاین نوبت پزشک با **سه نقش کامل** برای کاربران ایرانی:

| بخش | تکنولوژی |
|---|---|
| **بکاند** | Spring Boot 3.3 (Java 17) + Spring Security (JWT) + Spring Data JPA |
| **فرانتاند** | اندروید نیتیو (Java) + Material Design 3 + Retrofit |
| **دیتابیس** | **PostgreSQL** (پیش‌فرض) — با پروفایل H2 برای اجرای سریع |

پکیج‌ها: `ir.artor.badoki` (بکاند و اندروید)

---

## 👥 نقش‌ها و امکانات هر نقش

| قابلیت | 🧑 بیمار (PATIENT) | 👨‍⚕️ پزشک (DOCTOR) | 👑 ادمین (ADMIN) |
|---|---|---|---|
| جستجو و مشاهده پزشکان | ✅ | ❌ | ❌ |
| رزرو / لغو / تغییر زمان / حذف نوبت | ✅ | ❌ | ❌ |
| داشبورد بیمار (آمار + نوبت بعدی) | ✅ | ❌ | ❌ |
| مشاهده نوبت‌های بیماران + اطلاعات تماس بیمار | ❌ | ✅ | ❌ |
| تأیید نوبت (PENDING → CONFIRMED) | ❌ | ✅ | ❌ |
| علامت‌گذاری انجام‌شده (CONFIRMED → COMPLETED) | ❌ | ✅ | ❌ |
| لغو نوبت توسط پزشک | ❌ | ✅ | ❌ |
| ویرایش پروفایل پزشک (هزینه، ساعات کاری، روزها، بیو، آدرس) | ❌ | ✅ | ❌ |
| داشبورد پزشک (آمار امروز/پیش‌رو/انجام‌شده/لغو) | ❌ | ✅ | ❌ |
| CRUD کامل پزشکان (ساخت با حساب کاربری، ویرایش، حذف) | ❌ | ❌ | ✅ |
| مدیریت کاربران (مشاهده همه با نقش) | ❌ | ❌ | ✅ |
| آمار کل سیستم | ❌ | ❌ | ✅ |

### جریان کامل یک نوبت
1. بیمار نوبت می‌گیرد → وضعیت **PENDING** (در انتظار تأیید)
2. پزشک در پورتال خود نوبت را می‌بیند و **تأیید** می‌کند → **CONFIRMED**
3. بعد از ویزیت، پزشک **انجام‌شده** علامت می‌زند → **COMPLETED**
4. در هر مرحله (به‌جز انجام‌شده) پزشک یا بیمار می‌تواند **لغو** کند → **CANCELED**

---

## ✨ ویژگی‌های UI

- تم Material 3 با **حالت تاریک/روشن** + فونت **وزیرمتن** + کاملاً **RTL** با ارقام و تقویم شمسی
- **سایدبار هوشمند**: منو بر اساس نقش نمایش داده می‌شود
- حالت‌های **بارگذاری / خطا (تلاش دوباره) / خالی** با تصویرسازی اختصاصی
- **به‌روزرسانی خوش‌بینانه** (Optimistic UI) در: لغو/تغییر زمان/حذف نوبت، تغییر وضعیت توسط پزشک، ویرایش پروفایل — با بازگردانی خودکار در خطا
- آواتارهای رنگی خودکار، چیپ‌های فیلتر، کشیدن به پایین برای رفرش

---

## 🚀 راه‌اندازی

### ۱) پایگاه‌داده PostgreSQL
```bash
cd badoki
docker compose up -d          # postgres روی پورت 5432، کاربر/رمز: badoki/badoki
```
> بدون داکر؟ فقط یک PostgreSQL نصب کنید و دیتابیس `badoki` را بسازید.

### ۲) بکاند
```bash
cd backend
mvn spring-boot:run
```
- آدرس: http://localhost:8080 — **Swagger UI:** http://localhost:8080/swagger-ui.html
- داده‌های نمونه در اولین اجرا خودکار بارگذاری می‌شوند
- اجرای سریع بدون PostgreSQL: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`

### ۳) اپ اندروید
پوشه `android` را در Android Studio باز کنید و روی ایمولاتور اجرا کنید
(پیش‌فرض به `http://10.0.2.2:8080` وصل می‌شود؛ برای گوشی واقعی `BASE_URL` را در `ApiClient.java` عوض کنید).

APK آماده: **`Badoki-debug.apk`**

---

## 🔑 حساب‌های نمونه

| نقش | ایمیل | رمز |
|---|---|---|
| 👤 بیمار | `ali@example.com` | `123456` |
| 👨‍⚕️ پزشک (دکتر علی رضایی) | `doctor@example.com` | `123456` |
| 👨‍⚕️ پزشک (دکتر حسین کریمی) | `dr.reza@example.com` | `123456` |
| 👑 ادمین | `admin@badoki.ir` | `admin123` |

داده اولیه: ۱۸ پزشک ایرانی، ۱۰ کاربر (۴ حساب پزشک متصل)، ۱۱ نوبت با وضعیت‌های متنوع.

---

## 🔌 مستندات API

### عمومی
| متد | مسیر | توضیح |
|---|---|---|
| POST | `/api/auth/register` ، `/api/auth/login` | ثبت‌نام / ورود |
| GET | `/api/doctors?query=&specialty=&city=&hospital=` | لیست/جستجوی پزشکان (فیلترها با AND ترکیب می‌شوند؛ query روی نام، تخصص، شهر و بیمارستان) |
| GET | `/api/doctors/{id}` ، `/api/doctors/{id}/slots?date=` | جزئیات و اسلات‌های خالی |
| GET | `/api/meta/specialties` ، `/api/meta/cities` ، `/api/meta/hospitals` | فیلترهای صفحه جستجو |

### بیمار
| متد | مسیر | توضیح |
|---|---|---|
| GET | `/api/dashboard` | آمار + نوبت بعدی |
| GET | `/api/appointments?filter=all|upcoming|past|canceled` | نوبت‌های من |
| POST | `/api/appointments` | رزرو (وضعیت اولیه PENDING) |
| PUT | `/api/appointments/{id}` | تغییر زمان |
| PUT | `/api/appointments/{id}/cancel` | لغو |
| DELETE | `/api/appointments/{id}` | حذف |
| GET/PUT | `/api/me` | پروفایل |

### پزشک (نیاز به نقش DOCTOR)
| متد | مسیر | توضیح |
|---|---|---|
| GET/PUT | `/api/doctor/me` | مشاهده / ویرایش پروفایل خود |
| GET | `/api/doctor/dashboard` | آمار پزشک |
| GET | `/api/doctor/appointments?filter=all|upcoming|past` | نوبت‌های بیماران (با نام و موبایل بیمار) |
| PUT | `/api/doctor/appointments/{id}/status` | تغییر وضعیت: `CONFIRMED`، `COMPLETED`، `CANCELED` |

### ادمین (نیاز به نقش ADMIN)
| متد | مسیر | توضیح |
|---|---|---|
| GET | `/api/admin/doctors` | لیست همه پزشکان |
| POST | `/api/admin/doctors` | ساخت پزشک + (اختیاری) حساب DOCTOR با `email`/`password` |
| PUT/DELETE | `/api/admin/doctors/{id}` | ویرایش / حذف |
| GET | `/api/admin/users` | لیست کاربران با نقش |
| GET | `/api/admin/stats` | آمار کل سیستم |

---

## 🗂 ساختار پروژه

```
badoki/
├── backend/src/main/java/ir/artor/badoki/
│   ├── config/          # امنیت، سیدر
│   ├── controller/      # Auth, Doctor, Appointment, Profile,
│   │                    # DoctorPortal (پزشک), AdminDoctor, AdminUser
│   ├── dto/ model/ repository/ service/
│   └── security/        # JWT
├── android/app/src/main/java/ir/artor/badoki/
│   ├── api/             # Retrofit: ApiClient, ApiService, Models
│   ├── adapter/         # DoctorAdapter, AppointmentAdapter,
│   │                    # DoctorAppointmentAdapter, AdminDoctorAdapter
│   ├── ui/              # صفحات بیمار + پزشک (Dashboard/Appointments/Profile)
│   │                    # + صفحات ادمین (Dashboard/Doctors/Users)
│   └── util/            # تقویم جلالی، فرمت فارسی، نشست
├── docker-compose.yml   # PostgreSQL
└── Badoki-debug.apk
```

## 📝 نکات فنی
- **PostgreSQL** با `ddl-auto: update` — داده‌ها کاملاً ماندگارند (فایل `application-h2.yml` برای تست سریع)
- توکن JWT در هدر `Authorization: Bearer`؛ رمزها با BCrypt؛ کنترل دسترسی با `@PreAuthorize` و `hasRole`
- اعتبارسنجی اسلات‌ها: تاریخ گذشته/دور، روز غیرکاری، ساعت خارج از بازه، رزرو دوبل — همه با پیام فارسی
- تقویم شمسی (الگوریتم jalaali) با تست بازگشتی ۳۶٬۵۰۰ روزه
