#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""دیاگرام‌های UML گزارش بدوکی — چیدمان همان اسکریپت گزارش آرمین ترکمندی.
matplotlib 3.11 خودش فارسی را شکل می‌دهد؛ بنابراین reshape/bidi اینجا استفاده نمی‌شود
(اگر reshape شود متن برعکس دیده می‌شود)."""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
from matplotlib.patches import FancyBboxPatch, Ellipse, Rectangle, Circle, Polygon

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "figures")
os.makedirs(OUT, exist_ok=True)

FONT_REG = "/home/user/badoki/android/app/src/main/res/font/vazirmatn_regular.ttf"
FONT_BOLD = "/home/user/badoki/android/app/src/main/res/font/vazirmatn_bold.ttf"
fm.fontManager.addfont(FONT_REG)
fm.fontManager.addfont(FONT_BOLD)
FA = fm.FontProperties(fname=FONT_REG)
FA_B = fm.FontProperties(fname=FONT_BOLD)

TEAL = "#0F766E"
TEAL_BG = "#E6F7F4"
INK = "#17201F"
SLATE = "#334155"
AMBER = "#D97706"
AMBER_BG = "#FEF3C7"
GREEN_BG = "#DCFCE7"
RED_BG = "#FEE2E2"
RED = "#B91C1C"
GREEN = "#15803D"
SOFT = "#F8FCFB"
WHITE = "#FFFFFF"


def fa(t):
    return "" if t is None else str(t)


def new_ax(w=15, h=9.5):
    fig, ax = plt.subplots(figsize=(w, h))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis("off")
    return fig, ax


def box(ax, x, y, w, h, text, fc=TEAL_BG, ec=TEAL, fs=11, bold=False, tc=INK, rounded=True):
    style = "round,pad=0.8,rounding_size=1.5" if rounded else "square,pad=0.8"
    p = FancyBboxPatch((x, y), w, h, boxstyle=style,
                       linewidth=1.4, edgecolor=ec, facecolor=fc)
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, fa(text), ha="center", va="center",
            fontproperties=FA_B if bold else FA, fontsize=fs, color=tc)


def ellipse(ax, cx, cy, w, h, text, fc=WHITE, ec=TEAL, fs=10.5):
    e = Ellipse((cx, cy), w, h, linewidth=1.3, edgecolor=ec, facecolor=fc)
    ax.add_patch(e)
    ax.text(cx, cy, fa(text), ha="center", va="center",
            fontproperties=FA, fontsize=fs, color=INK)


def actor(ax, x, y, name, fs=11):
    ax.add_patch(plt.Circle((x, y + 8.5), 2.6, fill=True, facecolor=TEAL, edgecolor=TEAL))
    ax.plot([x, x], [y + 5.5, y - 2.5], color=TEAL, lw=1.8)
    ax.plot([x - 4, x + 4], [y + 3.5, y + 3.5], color=TEAL, lw=1.8)
    ax.plot([x, x - 3.5], [y - 2.5, y - 6.5], color=TEAL, lw=1.8)
    ax.plot([x, x + 3.5], [y - 2.5, y - 6.5], color=TEAL, lw=1.8)
    ax.text(x, y - 9.5, fa(name), ha="center", va="center", fontproperties=FA_B, fontsize=fs)


def line(ax, x1, y1, x2, y2, dashed=False):
    ax.plot([x1, x2], [y1, y2], color=SLATE, lw=1.2,
            linestyle="--" if dashed else "-")


def arrow(ax, x1, y1, x2, y2, text=None, color=TEAL, dashed=False, fs=9.5,
          text_dx=0, text_dy=0.9):
    ax.annotate(
        "",
        xy=(x2, y2), xytext=(x1, y1),
        arrowprops=dict(
            arrowstyle="->",
            color=color,
            lw=1.2,
            linestyle=(0, (3, 2)) if dashed else "-",
            mutation_scale=9,
        ),
    )
    if text:
        mx, my = (x1 + x2) / 2 + text_dx, (y1 + y2) / 2 + text_dy
        ax.text(mx, my, fa(text), ha="center", va="center",
                fontproperties=FA, fontsize=fs, color="#1E293B",
                bbox=dict(boxstyle="round,pad=0.25", fc="white", ec="none", alpha=0.9))


def diamond(ax, cx, cy, w, h, text, fs=10):
    verts = [(cx, cy + h / 2), (cx + w / 2, cy), (cx, cy - h / 2), (cx - w / 2, cy)]
    p = Polygon(verts, closed=True, facecolor=AMBER_BG, edgecolor=AMBER, linewidth=1.4)
    ax.add_patch(p)
    ax.text(cx, cy, fa(text), ha="center", va="center", fontproperties=FA, fontsize=fs, color="#431407")


def lifeline(ax, x, name, y_top=98, y_bot=6):
    box(ax, x - 8, y_top - 2.5, 16, 5.2, name, fc=TEAL, ec=TEAL, fs=10, bold=True, tc=WHITE)
    ax.plot([x, x], [y_top - 2.5, y_bot], color="#64748B", lw=1.0)
    return x


def save(fig, name):
    fig.savefig(os.path.join(OUT, name), dpi=170, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("saved:", name)


# =========================================================
# ۱) موارد کاربرد — سه ستون مثل گزارش آرمین
# =========================================================
def use_case():
    fig, ax = new_ax(16, 11)
    rect = Rectangle((14, 4), 70, 92, linewidth=1.6, edgecolor=TEAL,
                     facecolor=SOFT, linestyle="--")
    ax.add_patch(rect)
    ax.text(49, 93.5, fa("سامانه رزرو نوبت پزشک «بدوکی»"), ha="center", va="center",
            fontproperties=FA_B, fontsize=13, color=TEAL)

    actor(ax, 7, 55, "بیمار")
    actor(ax, 93.5, 72, "پزشک")
    actor(ax, 93.5, 20, "مدیر سیستم")

    for cy, t in ((84, "ثبت‌نام و ورود دومرحله‌ای"),
                  (71, "جستجو و مشاهده پزشکان"),
                  (58, "مشاهده اسلات‌های خالی"),
                  (45, "رزرو نوبت جدید"),
                  (32, "تغییر زمان / لغو نوبت"),
                  (19, "مشاهده نوبت‌های من")):
        ellipse(ax, 29, cy, 22, 9, t, fs=9.5)
        line(ax, 11.5, 55, 18, cy)

    for cy, t in ((86, "مشاهده نوبت‌های بیماران"),
                  (74, "تأیید / رد نوبت"),
                  (62, "ثبت نوبت انجام‌شده"),
                  (50, "لغو نوبت توسط پزشک"),
                  (38, "ویرایش پروفایل پزشک")):
        ellipse(ax, 51, cy, 20, 8.5, t, fs=9.2)
        line(ax, 89, 72, 61, cy)

    for cy, t in ((30, "افزودن / ویرایش / حذف پزشک"),
                  (18, "مدیریت کاربران و نقش‌ها"),
                  (8, "مشاهده آمار کل سیستم")):
        ellipse(ax, 74, cy, 22, 8.5, t, fs=9.0)
        line(ax, 89, 20, 85, cy)

    save(fig, "usecase.png")


# =========================================================
# ۲) توالی — سه خط عمر مثل گزارش آرمین
# =========================================================
def sequence():
    fig, ax = new_ax(15.5, 10.5)
    xs = {"app": 16, "server": 50, "db": 84}
    lifeline(ax, xs["app"], "بیمار (اپ اندروید)", y_top=96, y_bot=6)
    lifeline(ax, xs["server"], "سرور بدوکی (Spring Boot)", y_top=96, y_bot=6)
    lifeline(ax, xs["db"], "پایگاه داده (PostgreSQL)", y_top=96, y_bot=6)

    y = 86
    arrow(ax, xs["app"], y, xs["server"], y, "ورود با ایمیل و رمز عبور")
    y -= 7
    arrow(ax, xs["server"], y, xs["db"], y, "استعلام کاربر و بررسی BCrypt", fs=9)
    y -= 6.5
    arrow(ax, xs["db"], y, xs["server"], y, "اطلاعات کاربر", dashed=True, color="#64748B", fs=9)
    y -= 6.5
    arrow(ax, xs["server"], y, xs["app"], y, "ارسال OTP ؛ requiresOtp", dashed=True, color="#64748B")
    y -= 7
    arrow(ax, xs["app"], y, xs["server"], y, "تأیید کد و دریافت JWT")
    y -= 7
    arrow(ax, xs["app"], y, xs["server"], y, "GET پزشکان با شهر / تخصص / بیمارستان")
    y -= 6.5
    arrow(ax, xs["server"], y, xs["db"], y, "پرس‌وجوی فیلترشده", fs=9)
    y -= 6.5
    arrow(ax, xs["db"], y, xs["app"], y, "لیست پزشکان و اسلات‌ها", dashed=True, color="#64748B", fs=9)
    y -= 7
    arrow(ax, xs["app"], y, xs["server"], y, "POST ثبت نوبت")
    y -= 6.5
    arrow(ax, xs["server"], y, xs["db"], y, "ذخیره نوبت با وضعیت در انتظار تأیید", fs=9)
    y -= 6.5
    arrow(ax, xs["server"], y, xs["app"], y, "نوبت ثبت‌شده (PENDING)", dashed=True, color="#64748B")
    save(fig, "sequence.png")


# =========================================================
# ۳) فعالیت — همان جریان گزارش آرمین
# =========================================================
def activity():
    fig, ax = new_ax(13, 15)

    def node(x, y, w, h, text, fc=TEAL_BG):
        box(ax, x - w / 2, y - h / 2, w, h, text, fc=fc, fs=10.5)

    def down(x, y1, y2, label=None, lx=0):
        arrow(ax, x, y1, x, y2, label, text_dx=lx, fs=9)

    x = 50
    ax.add_patch(plt.Circle((x, 96.5), 2.0, fill=True, facecolor=TEAL, edgecolor=TEAL))
    down(x, 94.3, 89.2)
    node(x, 85.5, 36, 7, "ورود و احراز هویت بیمار")
    down(x, 81.8, 77.2)
    node(x, 73.5, 40, 7, "جستجوی پزشک و مشاهده اسلات‌های خالی")
    down(x, 69.8, 65.2)
    diamond(ax, x, 61.0, 24, 8.2, "روز کاری پزشک؟")
    arrow(ax, x - 12, 61.0, x - 28, 61.0, "خیر", text_dy=1.5, fs=9)
    node(x - 28, 53.0, 26, 6, "انتخاب تاریخ دیگر", fc="#F1F5F9")
    line(ax, x - 28, 49.8, x - 28, 73.5, dashed=True)
    line(ax, x - 28, 73.5, x - 20, 73.5, dashed=True)
    down(x, 56.7, 51.2, "بله", lx=4)
    node(x, 47.5, 40, 7, "انتخاب ساعت از اسلات‌های خالی")
    down(x, 43.8, 39.2)
    diamond(ax, x, 35.0, 24, 8.2, "اسلات خالی است؟")
    arrow(ax, x - 12, 35.0, x - 28, 35.0, "خیر", text_dy=1.5, fs=9)
    node(x - 28, 27.0, 26, 6, "انتخاب ساعت دیگر", fc="#F1F5F9")
    line(ax, x - 28, 23.8, x - 28, 47.5, dashed=True)
    line(ax, x - 28, 47.5, x - 20, 47.5, dashed=True)
    down(x, 30.7, 25.4, "بله", lx=4)
    node(x, 21.5, 42, 7, "ثبت نوبت — وضعیت: در انتظار تأیید", fc=AMBER_BG)
    down(x, 17.8, 13.2)
    node(x, 9.5, 42, 7, "تأیید پزشک، قطعی، ارائه خدمت، انجام‌شده", fc=GREEN_BG)
    save(fig, "activity.png")


# =========================================================
# ۴) کلاس — سه موجودیت + دو enum مثل گزارش آرمین
# =========================================================
def class_diag():
    fig, ax = new_ax(15.5, 10)

    def cls(ax, x, y, w, h, title, fields, fc=TEAL_BG):
        p = Rectangle((x, y), w, h, linewidth=1.5, edgecolor=TEAL, facecolor=fc)
        ax.add_patch(p)
        ax.plot([x, x + w], [y + h - 7, y + h - 7], color=TEAL, lw=1.2)
        ax.text(x + w / 2, y + h - 3.5, fa(title), ha="center", va="center",
                fontproperties=FA_B, fontsize=11.5, color=TEAL)
        for i, f in enumerate(fields):
            ax.text(x + 1.5, y + h - 10.2 - i * 4.0, fa(f), ha="left", va="center",
                    fontproperties=FA, fontsize=9.6, color=INK)

    cls(ax, 3, 36, 29, 50, "User (کاربر)", [
        "- id : Long",
        "- fullName : String",
        "- email : String",
        "- phone : String",
        "- passwordHash : String",
        "- role : Role",
        "- twoFactorEnabled : boolean",
        "- createdAt : Instant",
    ])
    cls(ax, 36, 32, 30, 54, "Doctor (پزشک)", [
        "- id : Long",
        "- fullName : String",
        "- specialty : String",
        "- city : String",
        "- hospitalName : String",
        "- visitPrice : long",
        "- rating : double",
        "- availableDays : String",
        "- startHour / endHour : int",
        "- userId : Long",
    ])
    cls(ax, 70, 40, 27, 46, "Appointment (نوبت)", [
        "- id : Long",
        "- date : LocalDate",
        "- time : LocalTime",
        "- status : AppointmentStatus",
        "- notes : String",
        "- createdAt : Instant",
    ])
    cls(ax, 3, 6, 29, 20, "«enum» Role", ["PATIENT", "DOCTOR", "ADMIN"], fc=AMBER_BG)
    cls(ax, 70, 6, 27, 24, "«enum» AppointmentStatus",
        ["PENDING", "CONFIRMED", "COMPLETED", "CANCELED"], fc=AMBER_BG)

    ax.plot([32, 36], [62, 62], color=SLATE, lw=1.3)
    ax.text(34, 88.6, fa("1 — 0..1 حساب پزشک"), ha="center", fontproperties=FA, fontsize=8.5, color=SLATE)
    ax.plot([66, 70], [62, 62], color=SLATE, lw=1.3)
    ax.text(68, 88.6, fa("1 — *"), ha="center", fontproperties=FA, fontsize=8.5, color=SLATE)
    ax.plot([17.5, 17.5], [36, 26], color="#94A3B8", lw=1.1, linestyle=":")
    ax.plot([83.5, 83.5], [40, 30], color="#94A3B8", lw=1.1, linestyle=":")
    save(fig, "class.png")


# =========================================================
# ۵) ER
# =========================================================
def er():
    fig, ax = new_ax(16, 11)

    def entity(x, y, w, h, title, rows):
        ax.add_patch(Rectangle((x, y), w, h, linewidth=1.4, edgecolor=TEAL, facecolor=WHITE))
        ax.add_patch(Rectangle((x, y + h - 7), w, 7, linewidth=0, facecolor=TEAL))
        ax.text(x + w / 2, y + h - 3.5, fa(title), ha="center", va="center",
                fontproperties=FA_B, fontsize=11, color=WHITE)
        for i, r in enumerate(rows):
            ax.text(x + 1.4, y + h - 10.4 - i * 3.6, fa(r), ha="left", va="center",
                    fontproperties=FA, fontsize=8.6, color=INK)

    entity(3, 50, 27, 40, "users", [
        "PK  id",
        "    full_name",
        "UQ  email",
        "    phone",
        "    password_hash",
        "    role",
        "    two_factor_enabled",
        "    created_at",
    ])
    entity(37, 46, 26, 44, "doctors", [
        "PK  id",
        "    full_name",
        "    specialty",
        "    city",
        "    hospital_name",
        "    visit_price",
        "    rating / review_count",
        "    available_days",
        "    start_hour / end_hour",
        "FK  user_id (یکتا)",
    ])
    entity(70, 52, 27, 38, "appointments", [
        "PK  id",
        "FK  patient_id",
        "FK  doctor_id",
        "    date , time",
        "    status",
        "    notes",
        "    created_at",
        "UQ (doctor_id, date, time)",
    ])
    entity(37, 6, 26, 26, "reviews", [
        "PK  id",
        "FK  patient_id / doctor_id",
        "    rating (1..5)",
        "    comment",
        "UQ (patient_id, doctor_id)",
    ])
    entity(70, 6, 27, 32, "notifications", [
        "PK  id",
        "FK  user_id",
        "    title , message",
        "    type",
        "    is_read",
        "    created_at",
    ])
    ax.plot([30, 37], [72, 72], color=SLATE, lw=1.25)
    ax.text(33.5, 94, fa("1 — 0..1 حساب پزشک"), ha="center", fontproperties=FA, fontsize=8.5, color=SLATE)
    ax.plot([63, 70], [72, 72], color=SLATE, lw=1.25)
    ax.text(66.5, 94, fa("1 — * نوبت"), ha="center", fontproperties=FA, fontsize=8.5, color=SLATE)
    ax.plot([50, 50], [46, 32], color=SLATE, lw=1.25)
    ax.text(52.2, 39, fa("1 — * نظر"), ha="left", fontproperties=FA, fontsize=8.5, color=SLATE)
    # مسیر اعلان: از زیر users، دور reviews، به notifications
    ax.plot([16.5, 16.5], [50, 2.0], color=SLATE, lw=1.15)
    ax.plot([16.5, 70], [2.0, 2.0], color=SLATE, lw=1.15)
    ax.plot([70, 70], [2.0, 10], color=SLATE, lw=1.15)
    ax.text(22, 3.6, fa("1 — * اعلان"), ha="left", fontproperties=FA, fontsize=8.5, color=SLATE)
    save(fig, "er.png")


def architecture():
    fig, ax = new_ax(16, 8.4)
    layers = [
        (6, 74, 88, 16, "لایه ارائه — اپلیکیشن اندروید (Java، Material 3، Retrofit)", TEAL_BG),
        (6, 52, 88, 16, "لایه API — REST + JWT + RBAC  |  Spring Security  |  Swagger", "#D9F1ED"),
        (6, 30, 56, 16, "لایه کسب‌وکار — Auth / OTP / Appointment / Review / Notification", AMBER_BG),
        (64, 30, 30, 16, "زمان‌بند انقضا + SMTP جیمیل", "#FEF9C3"),
        (6, 8, 88, 16, "لایه داده — Spring Data JPA / Hibernate / PostgreSQL", GREEN_BG),
    ]
    for x, y, w, h, t, fc in layers:
        box(ax, x, y, w, h, t, fc=fc, fs=10.5)
    arrow(ax, 50, 73.6, 50, 68.4)
    arrow(ax, 50, 51.6, 50, 46.4)
    arrow(ax, 50, 29.6, 50, 24.4)
    ax.text(50, 96, fa("معماری لایه‌ای سامانه بدوکی"), ha="center",
            fontproperties=FA_B, fontsize=13, color=TEAL)
    save(fig, "architecture.png")


# ===========================    save(fig, "architecture.png")


# =========================================================
# ۷) وضعیت نوبت
# =========================================================
def state():
    fig, ax = new_ax(15.6, 7.2)
    box(ax, 6, 40, 22, 16, "PENDING\nدر انتظار تأیید", fc=AMBER_BG, ec=AMBER, fs=11, bold=True)
    box(ax, 40, 40, 22, 16, "CONFIRMED\nقطعی", fc=GREEN_BG, ec=GREEN, fs=11, bold=True)
    box(ax, 74, 40, 22, 16, "COMPLETED\nانجام‌شده", fc="#DBEAFE", ec="#1D4ED8", fs=11, bold=True)
    box(ax, 40, 8, 22, 14, "CANCELED\nلغو / منقضی", fc=RED_BG, ec=RED, fs=11, bold=True)
    ax.add_patch(Circle((3.2, 48), 1.4, fill=True, facecolor=TEAL, edgecolor=TEAL))
    arrow(ax, 4.7, 48, 6.0, 48)
    arrow(ax, 28, 48, 40, 48, "تأیید پزشک", fs=8.5, text_dy=1.5)
    arrow(ax, 62, 48, 74, 48, "ویزیت / انقضای قطعی", fs=8.5, text_dy=1.5)
    arrow(ax, 17, 40, 40, 22, "لغو یا عدم تأیید تا تاریخ", fs=8.2, text_dx=-3, text_dy=-0.4)
    arrow(ax, 51, 40, 51, 22, "لغو پزشک / بیمار", fs=8.2, text_dx=9)
    save(fig, "state.png")


if __name__ == "__main__":
    use_case()
    sequence()
    activity()
    class_diag()
    er()
    architecture()
    state()
    print("ALL DIAGRAMS DONE")
