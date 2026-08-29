#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""منابع PlantUML گزارش بدوکی — reshape فقط برای رندر PlantUML است، نه matplotlib."""
import os

try:
    import arabic_reshaper
    from bidi.algorithm import get_display

    def fa(t):
        if t is None:
            return ""
        return get_display(arabic_reshaper.reshape(str(t)))
except Exception:
    def fa(t):
        return "" if t is None else str(t)

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "plantuml")
os.makedirs(OUT, exist_ok=True)


def write(name, body):
    path = os.path.join(OUT, name)
    with open(path, "w", encoding="utf-8") as f:
        f.write(body)
    print("saved:", path)


def usecase():
    write("usecase.puml", f"""@startuml
skinparam shadowing false
skinparam backgroundColor #FFFFFF
skinparam actorStyle awesome
left to right direction
actor "{fa("بیمار")}" as Patient
actor "{fa("پزشک")}" as Doctor
actor "{fa("مدیر سیستم")}" as Admin
rectangle "{fa("سامانه رزرو نوبت پزشک بدوکی")}" {{
  usecase "{fa("ثبت‌نام و ورود دومرحله‌ای")}" as UC1
  usecase "{fa("جستجو و مشاهده پزشکان")}" as UC2
  usecase "{fa("مشاهده اسلات‌های خالی")}" as UC3
  usecase "{fa("رزرو نوبت جدید")}" as UC4
  usecase "{fa("تغییر زمان / لغو نوبت")}" as UC5
  usecase "{fa("مشاهده نوبت‌های من")}" as UC6
  usecase "{fa("مشاهده نوبت‌های بیماران")}" as UC7
  usecase "{fa("تأیید / رد نوبت")}" as UC8
  usecase "{fa("ثبت نوبت انجام‌شده")}" as UC9
  usecase "{fa("لغو نوبت توسط پزشک")}" as UC10
  usecase "{fa("ویرایش پروفایل پزشک")}" as UC11
  usecase "{fa("افزودن / ویرایش / حذف پزشک")}" as UC12
  usecase "{fa("مدیریت کاربران و نقش‌ها")}" as UC13
  usecase "{fa("مشاهده آمار کل سیستم")}" as UC14
}}
Patient --> UC1
Patient --> UC2
Patient --> UC3
Patient --> UC4
Patient --> UC5
Patient --> UC6
Doctor --> UC7
Doctor --> UC8
Doctor --> UC9
Doctor --> UC10
Doctor --> UC11
Admin --> UC12
Admin --> UC13
Admin --> UC14
@enduml
""")


def sequence():
    write("sequence.puml", f"""@startuml
skinparam shadowing false
actor "{fa("بیمار (اپ اندروید)")}" as App
participant "{fa("سرور بدوکی (Spring Boot)")}" as S
database "{fa("پایگاه داده (PostgreSQL)")}" as DB
App -> S : {fa("ورود با ایمیل و رمز عبور")}
S -> DB : {fa("استعلام کاربر و بررسی BCrypt")}
DB --> S : {fa("اطلاعات کاربر")}
S --> App : {fa("ارسال OTP ؛ requiresOtp")}
App -> S : {fa("تأیید کد و دریافت JWT")}
App -> S : {fa("GET پزشکان با شهر / تخصص / بیمارستان")}
S -> DB : {fa("پرس‌وجوی فیلترشده")}
DB --> App : {fa("لیست پزشکان و اسلات‌ها")}
App -> S : {fa("POST ثبت نوبت")}
S -> DB : {fa("ذخیره نوبت با وضعیت در انتظار تأیید")}
S --> App : {fa("نوبت ثبت‌شده (PENDING)")}
@enduml
""")


def activity():
    write("activity.puml", f"""@startuml
skinparam shadowing false
start
:{fa("ورود و احراز هویت بیمار")};
:{fa("جستجوی پزشک و مشاهده اسلات‌های خالی")};
if ({fa("روز کاری پزشک؟")}) then ({fa("بله")})
  :{fa("انتخاب ساعت از اسلات‌های خالی")};
  if ({fa("اسلات خالی است؟")}) then ({fa("بله")})
    :{fa("ثبت نوبت — وضعیت: در انتظار تأیید")};
    :{fa("تأیید پزشک، قطعی، ارائه خدمت، انجام‌شده")};
    stop
  else ({fa("خیر")})
    :{fa("انتخاب ساعت دیگر")};
  endif
else ({fa("خیر")})
  :{fa("انتخاب تاریخ دیگر")};
endif
@enduml
""")


def class_diag():
    write("class.puml", f"""@startuml
skinparam shadowing false
skinparam classAttributeIconSize 0
class "{fa("User (کاربر)")}" as User {{
  - id : Long
  - fullName : String
  - email : String
  - phone : String
  - passwordHash : String
  - role : Role
  - twoFactorEnabled : boolean
  - createdAt : Instant
}}
class "{fa("Doctor (پزشک)")}" as Doctor {{
  - id : Long
  - fullName : String
  - specialty : String
  - city : String
  - hospitalName : String
  - visitPrice : long
  - rating : double
  - availableDays : String
  - startHour / endHour : int
  - userId : Long
}}
class "{fa("Appointment (نوبت)")}" as Appointment {{
  - id : Long
  - date : LocalDate
  - time : LocalTime
  - status : AppointmentStatus
  - notes : String
  - createdAt : Instant
}}
enum Role {{
  PATIENT
  DOCTOR
  ADMIN
}}
enum AppointmentStatus {{
  PENDING
  CONFIRMED
  COMPLETED
  CANCELED
}}
User "1" -- "0..1" Doctor
Doctor "1" -- "*" Appointment
User -- Role
Appointment -- AppointmentStatus
@enduml
""")


def er():
    write("er.puml", f"""@startuml
skinparam shadowing false
entity users {{
  * id : BIGINT <<PK>>
  --
  full_name
  * email <<UQ>>
  phone
  password_hash
  role
  two_factor_enabled
  created_at
}}
entity doctors {{
  * id : BIGINT <<PK>>
  --
  full_name
  specialty
  city
  hospital_name
  visit_price
  rating / review_count
  available_days
  start_hour / end_hour
  user_id <<FK,UQ>>
}}
entity appointments {{
  * id : BIGINT <<PK>>
  --
  patient_id <<FK>>
  doctor_id <<FK>>
  date
  time
  status
  notes
  created_at
  <<UQ (doctor_id, date, time)>>
}}
entity reviews {{
  * id : BIGINT <<PK>>
  --
  patient_id <<FK>>
  doctor_id <<FK>>
  rating
  comment
  <<UQ (patient_id, doctor_id)>>
}}
entity notifications {{
  * id : BIGINT <<PK>>
  --
  user_id <<FK>>
  title
  message
  type
  is_read
  created_at
}}
users ||--o| doctors
doctors ||--o{{ appointments
users ||--o{{ appointments
doctors ||--o{{ reviews
users ||--o{{ reviews
users ||--o{{ notifications
@enduml
""")


def architecture():
    write("architecture.puml", f"""@startuml
skinparam shadowing false
rectangle "{fa("لایه ارائه — اپلیکیشن اندروید (Java، Material 3، Retrofit)")}" as UI
rectangle "{fa("لایه API — REST + JWT + RBAC | Spring Security | Swagger")}" as API
rectangle "{fa("لایه کسب‌وکار — Auth / OTP / Appointment / Review / Notification")}" as SVC
rectangle "{fa("زمان‌بند انقضا + SMTP جیمیل")}" as JOB
rectangle "{fa("لایه داده — Spring Data JPA / Hibernate / PostgreSQL")}" as DATA
UI --> API
API --> SVC
API --> JOB
SVC --> DATA
JOB --> DATA
@enduml
""")


def state():
    write("state.puml", f"""@startuml
skinparam shadowing false
[*] --> PENDING : {fa("رزرو بیمار")}
PENDING --> CONFIRMED : {fa("تأیید پزشک")}
PENDING --> CANCELED : {fa("لغو یا عدم تأیید تا تاریخ")}
CONFIRMED --> COMPLETED : {fa("ویزیت / انقضای قطعی")}
CONFIRMED --> CANCELED : {fa("لغو پزشک / بیمار")}
PENDING : {fa("در انتظار تأیید")}
CONFIRMED : {fa("قطعی")}
COMPLETED : {fa("انجام‌شده")}
CANCELED : {fa("لغو / منقضی")}
@enduml
""")


if __name__ == "__main__":
    usecase()
    sequence()
    activity()
    class_diag()
    er()
    architecture()
    state()
    print("ALL PUML DONE")
