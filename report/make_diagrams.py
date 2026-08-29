#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""دیاگرام‌های UML گزارش بدوکی — فونت وزیرمتن و شکل‌دهی فارسی"""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
from matplotlib.patches import FancyBboxPatch, Ellipse, Rectangle, FancyArrowPatch, Circle, Polygon
import arabic_reshaper
from bidi.algorithm import get_display

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
TEAL_DK = "#115E59"
INK = "#17201F"
SLATE = "#334155"
AMBER = "#D97706"
AMBER_BG = "#FEF3C7"
GREEN_BG = "#DCFCE7"
GREEN = "#15803D"
RED_BG = "#FEE2E2"
RED = "#B91C1C"
SOFT = "#F8FCFB"
WHITE = "#FFFFFF"
LINE = "#475569"


def fa(t):
    """matplotlib 3.11 با فونت وزیرمتن خودش شکل‌دهی فارسی را درست انجام می‌دهد."""
    return "" if t is None else str(t)


def new_ax(w, h):
    fig, ax = plt.subplots(figsize=(w, h))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis("off")
    fig.tight_layout(pad=0.4)
    return fig, ax


def box(ax, x, y, w, h, text, fc=TEAL_BG, ec=TEAL, fs=10, bold=False, tc=INK, radius=1.4):
    p = FancyBboxPatch((x, y), w, h, boxstyle=f"round,pad=0.35,rounding_size={radius}",
                       linewidth=1.35, edgecolor=ec, facecolor=fc)
    ax.add_patch(p)
    if text:
        ax.text(x + w / 2, y + h / 2, fa(text), ha="center", va="center",
                fontproperties=FA_B if bold else FA, fontsize=fs, color=tc,
                wrap=True)


def ellipse(ax, cx, cy, w, h, text, fc=WHITE, ec=TEAL, fs=9):
    e = Ellipse((cx, cy), w, h, linewidth=1.25, edgecolor=ec, facecolor=fc)
    ax.add_patch(e)
    ax.text(cx, cy, fa(text), ha="center", va="center",
            fontproperties=FA, fontsize=fs, color=INK)


def actor(ax, x, y, name, fs=10.5):
    ax.add_patch(Circle((x, y + 7.2), 2.15, fill=True, facecolor=TEAL, edgecolor=TEAL))
    ax.plot([x, x], [y + 5.0, y - 1.6], color=TEAL, lw=1.8)
    ax.plot([x - 3.3, x + 3.3], [y + 3.2, y + 3.2], color=TEAL, lw=1.8)
    ax.plot([x, x - 2.8], [y - 1.6, y - 5.4], color=TEAL, lw=1.8)
    ax.plot([x, x + 2.8], [y - 1.6, y - 5.4], color=TEAL, lw=1.8)
    ax.text(x, y - 8.0, fa(name), ha="center", va="center", fontproperties=FA_B, fontsize=fs, color=TEAL_DK)


def line(ax, x1, y1, x2, y2, dashed=False, color=LINE, lw=1.05):
    ax.plot([x1, x2], [y1, y2], color=color, lw=lw, linestyle="--" if dashed else "-")


def arrow(ax, x1, y1, x2, y2, text=None, color=TEAL, dashed=False, fs=8.2,
          text_dx=0, text_dy=1.15, both=False):
    ax.annotate(
        "",
        xy=(x2, y2), xytext=(x1, y1),
        arrowprops=dict(
            arrowstyle="->",
            color=color,
            lw=1.05,
            linestyle=(0, (3, 2)) if dashed else "-",
            mutation_scale=8,
        ),
    )
    if text:
        mx, my = (x1 + x2) / 2 + text_dx, (y1 + y2) / 2 + text_dy
        ax.text(mx, my, fa(text), ha="center", va="center",
                fontproperties=FA, fontsize=fs, color=SLATE,
                bbox=dict(boxstyle="round,pad=0.18", fc=WHITE, ec="none", alpha=0.94))


def diamond(ax, cx, cy, w, h, text, fs=9):
    verts = [(cx, cy + h / 2), (cx + w / 2, cy), (cx, cy - h / 2), (cx - w / 2, cy)]
    p = Polygon(verts, closed=True, facecolor=AMBER_BG, edgecolor=AMBER, linewidth=1.35)
    ax.add_patch(p)
    ax.text(cx, cy, fa(text), ha="center", va="center", fontproperties=FA, fontsize=fs, color="#431407")


def save(fig, name):
    path = os.path.join(OUT, name)
    fig.savefig(path, dpi=175, bbox_inches="tight", facecolor=WHITE, pad_inches=0.25)
    plt.close(fig)
    print("saved", name)


def cls_box(ax, x, y, w, h, title, fields, stereotype=None, fc=TEAL_BG):
    ax.add_patch(Rectangle((x, y), w, h, linewidth=1.4, edgecolor=TEAL, facecolor=fc))
    header_h = 7.2 if stereotype else 6.2
    ax.plot([x, x + w], [y + h - header_h, y + h - header_h], color=TEAL, lw=1.15)
    title_y = y + h - 2.4 if stereotype else y + h - 3.1
    if stereotype:
        ax.text(x + w / 2, y + h - 2.0, fa(stereotype), ha="center", va="center",
                fontproperties=FA, fontsize=8, color=TEAL)
        ax.text(x + w / 2, y + h - 4.6, fa(title), ha="center", va="center",
                fontproperties=FA_B, fontsize=11, color=TEAL_DK)
    else:
        ax.text(x + w / 2, title_y, fa(title), ha="center", va="center",
                fontproperties=FA_B, fontsize=11, color=TEAL_DK)
    for i, f in enumerate(fields):
        ax.text(x + 1.3, y + h - header_h - 3.3 - i * 3.7, fa(f), ha="left", va="center",
                fontproperties=FA, fontsize=8.6, color=INK)


# =========================================================
# ۱) موارد کاربرد
# =========================================================
def use_case():
    fig, ax = new_ax(17.5, 11.2)
    ax.add_patch(Rectangle((16, 5), 68, 90, linewidth=1.55, edgecolor=TEAL,
                           facecolor=SOFT, linestyle="--"))
    ax.text(50, 91.5, fa("سامانه رزرو نوبت پزشک «بدوکی»"), ha="center", va="center",
            fontproperties=FA_B, fontsize=13, color=TEAL)

    actor(ax, 7.5, 52, "بیمار")
    actor(ax, 93, 74, "پزشک")
    actor(ax, 93, 24, "مدیر سیستم")

    p_uc = [
        (29, 80, "ثبت‌نام / ورود دومرحله‌ای"),
        (29, 67.5, "جستجوی پزشک\n(شهر، تخصص، بیمارستان)"),
        (29, 55, "رزرو نوبت"),
        (29, 43.5, "تغییر زمان / لغو نوبت"),
        (29, 32, "ثبت امتیاز و نظر"),
        (29, 20.5, "اطلاع‌رسانی و آموزش"),
    ]
    for cx, cy, t in p_uc:
        ellipse(ax, cx, cy, 20.5, 9.0, t, fs=8.4)
        line(ax, 11.2, 52, 18.8, cy)

    d_uc = [
        (50.5, 78, "مشاهده نوبت بیماران"),
        (50.5, 64, "تأیید / رد نوبت"),
        (50.5, 50, "ثبت نوبت انجام‌شده"),
        (50.5, 36, "ویرایش پروفایل پزشک"),
    ]
    for cx, cy, t in d_uc:
        ellipse(ax, cx, cy, 20.5, 9.0, t, fs=8.4)
        line(ax, 89.5, 74, 60.7, cy)

    a_uc = [
        (72, 48, "افزودن / ویرایش / حذف پزشک"),
        (72, 33, "مدیریت کاربران و نقش‌ها"),
        (72, 18, "مشاهده آمار کل سیستم"),
    ]
    for cx, cy, t in a_uc:
        ellipse(ax, cx, cy, 21.5, 9.2, t, fs=8.3)
        line(ax, 89.5, 24, 82.7, cy)

    ax.text(29, 87.4, fa("امکانات بیمار"), ha="center", fontproperties=FA_B, fontsize=8.5, color="#64748B")
    ax.text(50.5, 85.6, fa("امکانات پزشک"), ha="center", fontproperties=FA_B, fontsize=8.5, color="#64748B")
    ax.text(72, 55.6, fa("امکانات مدیر"), ha="center", fontproperties=FA_B, fontsize=8.5, color="#64748B")
    save(fig, "usecase.png")


# =========================================================
# ۲) توالی
# =========================================================
def sequence():
    fig, ax = new_ax(16.2, 12.4)
    xs = {"app": 14, "api": 40, "svc": 66, "db": 88}
    names = {
        "app": "اپ اندروید",
        "api": "کنترلر REST",
        "svc": "لایه سرویس",
        "db": "PostgreSQL",
    }
    for k, x in xs.items():
        box(ax, x - 7.2, 93.2, 14.4, 5.4, names[k], fc=TEAL, ec=TEAL, fs=9.5, bold=True, tc=WHITE, radius=0.8)
        ax.plot([x, x], [93.2, 5], color="#94A3B8", lw=1.05, linestyle="-")

    steps = [
        (86, "app", "api", "۱. POST /api/auth/login", False),
        (80, "api", "svc", "۲. بررسی ایمیل و BCrypt", False),
        (74.5, "svc", "db", "۳. خواندن کاربر", False),
        (69, "db", "svc", "۴. رکورد کاربر", True),
        (63.5, "svc", "api", "۵. ارسال OTP به ایمیل", True),
        (58, "api", "app", "۶. requiresOtp = true", True),
        (52, "app", "api", "۷. POST /verify-login", False),
        (46.5, "api", "app", "۸. توکن JWT", True),
        (40.5, "app", "api", "۹. GET /api/doctors?city&specialty&hospital", False),
        (34.5, "api", "db", "۱۰. جستجوی فیلترشده", False),
        (29, "db", "api", "۱۱. فهرست پزشکان", True),
        (23, "app", "api", "۱۲. POST /api/appointments", False),
        (17, "api", "svc", "۱۳. اعتبارسنجی اسلات + قفل یکتایی", False),
        (11.2, "svc", "db", "۱۴. ذخیره نوبت PENDING", False),
        (6.2, "api", "app", "۱۵. نوبت ثبت‌شده", True),
    ]
    for y, a, b, txt, dashed in steps:
        col = "#64748B" if dashed else TEAL
        arrow(ax, xs[a], y, xs[b], y, txt, color=col, dashed=dashed, fs=8.0, text_dy=1.35)
    save(fig, "sequence.png")


# =========================================================
# ۳) فعالیت
# =========================================================
def activity():
    fig, ax = new_ax(11.5, 16.2)

    def node(x, y, w, h, text, fc=TEAL_BG):
        box(ax, x - w / 2, y - h / 2, w, h, text, fc=fc, fs=10)

    x = 52
    ax.add_patch(Circle((x, 97.2), 1.7, fill=True, facecolor=TEAL, edgecolor=TEAL))
    ax.text(x + 5, 97.2, fa("شروع"), ha="left", va="center", fontproperties=FA, fontsize=9, color=SLATE)
    arrow(ax, x, 95.3, x, 92.2)
    node(x, 89.0, 40, 5.8, "ورود دومرحله‌ای بیمار")
    arrow(ax, x, 86.0, x, 82.6)
    node(x, 79.6, 44, 5.8, "جستجو با تخصص / شهر / بیمارستان")
    arrow(ax, x, 76.6, x, 73.2)
    node(x, 70.2, 40, 5.8, "انتخاب پزشک و مشاهده اسلات‌ها")
    arrow(ax, x, 67.2, x, 63.6)
    diamond(ax, x, 59.4, 28, 8.2, "روز کاری است؟")

    # خیر — سمت چپ
    arrow(ax, x - 14, 59.4, x - 28, 59.4, "خیر", text_dy=1.6, fs=8.5)
    node(x - 28, 52.4, 26, 5.2, "انتخاب تاریخ دیگر", fc="#F1F5F9")
    ax.annotate("", xy=(x, 67.0), xytext=(x - 28, 49.6),
                arrowprops=dict(arrowstyle="-|>", color=LINE, lw=1.15,
                                connectionstyle="arc3,rad=0.25"))

    arrow(ax, x, 55.2, x, 51.4, "بله", text_dx=3.2, fs=8.5)
    node(x, 47.8, 40, 5.8, "انتخاب ساعت از اسلات‌های خالی")
    arrow(ax, x, 44.8, x, 41.2)
    diamond(ax, x, 36.8, 28, 8.2, "اسلات خالی است؟")

    arrow(ax, x - 14, 36.8, x - 28, 36.8, "خیر", text_dy=1.6, fs=8.5)
    node(x - 28, 29.6, 26, 5.2, "انتخاب ساعت دیگر", fc="#F1F5F9")
    ax.annotate("", xy=(x, 44.6), xytext=(x - 28, 26.8),
                arrowprops=dict(arrowstyle="-|>", color=LINE, lw=1.15,
                                connectionstyle="arc3,rad=0.25"))

    arrow(ax, x, 32.6, x, 28.8, "بله", text_dx=3.2, fs=8.5)
    node(x, 25.4, 44, 5.8, "ثبت نوبت — وضعیت: در انتظار تأیید", fc=AMBER_BG)
    arrow(ax, x, 22.4, x, 18.8)
    node(x, 15.6, 44, 5.8, "تأیید پزشک، نوبت قطعی و اعلان بیمار", fc=GREEN_BG)
    arrow(ax, x, 12.6, x, 9.2)
    node(x, 6.0, 44, 5.8, "ارائه خدمت یا انقضای خودکار: انجام‌شده", fc=GREEN_BG)
    ax.add_patch(Circle((x, 1.7), 1.7, fill=True, facecolor=INK, edgecolor=INK))
    ax.add_patch(Circle((x, 1.7), 0.85, fill=True, facecolor=WHITE, edgecolor=INK, lw=0.4))
    save(fig, "activity.png")


# =========================================================
# ۴) کلاس
# =========================================================
def class_diag():
    fig, ax = new_ax(17.4, 11.0)
    cls_box(ax, 2, 38, 22, 50, "User", [
        "- id : Long",
        "- fullName : String",
        "- email : String",
        "- phone : String",
        "- passwordHash : String",
        "- role : Role",
        "- twoFactorEnabled : boolean",
        "- createdAt : Instant",
    ])
    cls_box(ax, 28, 32, 24, 56, "Doctor", [
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
    cls_box(ax, 56, 42, 20, 46, "Appointment", [
        "- id : Long",
        "- date : LocalDate",
        "- time : LocalTime",
        "- status : Status",
        "- notes : String",
        "- createdAt : Instant",
    ])
    cls_box(ax, 79, 54, 19, 34, "Review", [
        "- id : Long",
        "- rating : int",
        "- comment : String",
        "- createdAt : Instant",
    ])
    cls_box(ax, 79, 14, 19, 34, "Notification", [
        "- id : Long",
        "- title : String",
        "- message : String",
        "- type : String",
        "- read : boolean",
        "- createdAt : Instant",
    ])
    cls_box(ax, 2, 8, 22, 22, "Role", ["PATIENT", "DOCTOR", "ADMIN"],
            stereotype="«enum»", fc=AMBER_BG)
    cls_box(ax, 56, 8, 20, 26, "AppointmentStatus",
            ["PENDING", "CONFIRMED", "COMPLETED", "CANCELED"],
            stereotype="«enum»", fc=AMBER_BG)

    ax.plot([24, 28], [58, 58], color=SLATE, lw=1.25)
    ax.text(26, 60.4, fa("1 — 0..1"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([52, 56], [58, 58], color=SLATE, lw=1.25)
    ax.text(54, 60.4, fa("1 — *"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([76, 79], [70, 70], color=SLATE, lw=1.25)
    ax.text(77.5, 72.2, fa("1 — *"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([13, 13], [38, 30], color="#94A3B8", lw=1.0, linestyle=":")
    ax.plot([66, 66], [42, 34], color="#94A3B8", lw=1.0, linestyle=":")
    ax.text(13, 34, fa("نقش"), ha="center", fontproperties=FA, fontsize=7.5, color="#64748B")
    save(fig, "class.png")


# =========================================================
# ۵) ER
# =========================================================
def er():
    fig, ax = new_ax(16.8, 11.2)

    def entity(x, y, w, h, title, rows):
        ax.add_patch(Rectangle((x, y), w, h, linewidth=1.4, edgecolor=TEAL, facecolor=WHITE))
        ax.add_patch(Rectangle((x, y + h - 7), w, 7, linewidth=0, facecolor=TEAL))
        ax.text(x + w / 2, y + h - 3.5, fa(title), ha="center", va="center",
                fontproperties=FA_B, fontsize=11, color=WHITE)
        for i, r in enumerate(rows):
            ax.text(x + 1.4, y + h - 10.2 - i * 3.55, fa(r), ha="left", va="center",
                    fontproperties=FA, fontsize=8.4, color=INK)

    entity(4, 48, 28, 46, "users", [
        "PK  id",
        "    full_name",
        "UQ  email",
        "    phone",
        "    password_hash",
        "    role",
        "    two_factor_enabled",
        "    created_at",
    ])
    entity(38, 42, 26, 52, "doctors", [
        "PK  id",
        "    full_name",
        "    specialty",
        "    city",
        "    hospital_name",
        "    visit_price",
        "    rating / review_count",
        "    available_days",
        "    start_hour / end_hour",
        "FK  user_id  (یکتا)",
    ])
    entity(70, 50, 26, 44, "appointments", [
        "PK  id",
        "FK  patient_id  users",
        "FK  doctor_id  doctors",
        "    date , time",
        "    status",
        "    notes",
        "    created_at",
        "UQ  (doctor_id, date, time)",
    ])
    entity(38, 4, 26, 30, "reviews", [
        "PK  id",
        "FK  patient_id",
        "FK  doctor_id",
        "    rating (1..5)",
        "    comment",
        "UQ  (patient_id, doctor_id)",
    ])
    entity(70, 8, 26, 32, "notifications", [
        "PK  id",
        "FK  user_id  users",
        "    title , message",
        "    type",
        "    is_read",
        "    created_at",
    ])

    ax.plot([32, 38], [72, 72], color=SLATE, lw=1.25)
    ax.text(35, 74.2, fa("1 — 0..1 حساب"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([64, 70], [72, 72], color=SLATE, lw=1.25)
    ax.text(67, 74.2, fa("1 — * پزشک"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([18, 18], [94, 97], color=SLATE, lw=1.25)
    ax.plot([18, 83], [97, 97], color=SLATE, lw=1.25)
    ax.plot([83, 83], [97, 94], color=SLATE, lw=1.25)
    ax.text(50, 98.6, fa("1 بیمار — * نوبت"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([51, 51], [42, 34], color=SLATE, lw=1.25)
    ax.text(54.5, 38, fa("1 — * نظر"), ha="left", fontproperties=FA, fontsize=8, color=SLATE)

    ax.plot([18, 18], [48, 20], color=SLATE, lw=1.15)
    ax.plot([18, 70], [20, 20], color=SLATE, lw=1.15)
    ax.text(44, 21.8, fa("1 — * اعلان"), ha="center", fontproperties=FA, fontsize=8, color=SLATE)
    save(fig, "er.png")


# =========================================================
# ۶) معماری
# =========================================================
def architecture():
    fig, ax = new_ax(16.4, 8.6)
    layers = [
        (6, 72, 88, 18, "لایه ارائه — اپلیکیشن اندروید (Java، Material 3، Retrofit، سایدبار نقش‌محور)", TEAL_BG),
        (6, 50, 88, 16, "لایه API — REST + JWT + RBAC  |  Spring Security  |  Swagger", "#D9F1ED"),
        (6, 28, 56, 16, "لایه کسب‌وکار — Auth / OTP / Appointment / Review / Notification / Doctor", AMBER_BG),
        (64, 28, 30, 16, "زمان‌بند انقضا + SMTP جیمیل", "#FEF9C3"),
        (6, 6, 88, 16, "لایه داده — Spring Data JPA / Hibernate  →  PostgreSQL", GREEN_BG),
    ]
    for x, y, w, h, t, fc in layers:
        box(ax, x, y, w, h, t, fc=fc, fs=10.5, radius=1.2)
    for y in (72, 50, 28):
        arrow(ax, 50, y, 50, y - 4.2, color=TEAL)
    ax.text(50, 96.2, fa("معماری لایه‌ای سامانه بدوکی"), ha="center",
            fontproperties=FA_B, fontsize=13, color=TEAL)
    save(fig, "architecture.png")


# =========================================================
# ۷) وضعیت نوبت
# =========================================================
def state():
    fig, ax = new_ax(15.6, 7.4)
    box(ax, 6, 38, 20, 16, "PENDING\nدر انتظار تأیید", fc=AMBER_BG, ec=AMBER, fs=11, bold=True)
    box(ax, 40, 38, 20, 16, "CONFIRMED\nقطعی", fc=GREEN_BG, ec=GREEN, fs=11, bold=True)
    box(ax, 74, 38, 20, 16, "COMPLETED\nانجام‌شده", fc="#DBEAFE", ec="#1D4ED8", fs=11, bold=True)
    box(ax, 40, 8, 20, 14, "CANCELED\nلغو / منقضی", fc=RED_BG, ec=RED, fs=11, bold=True)
    ax.add_patch(Circle((3.0, 46), 1.35, fill=True, facecolor=TEAL, edgecolor=TEAL))
    arrow(ax, 4.4, 46, 6.0, 46, color=TEAL)

    arrow(ax, 26, 46, 40, 46, "تأیید پزشک", fs=8.5, text_dy=1.5)
    arrow(ax, 60, 46, 74, 46, "ویزیت / انقضای قطعی", fs=8.5, text_dy=1.5)
    arrow(ax, 16, 38, 40, 22, "لغو یا عدم تأیید تا تاریخ", fs=8.2, text_dx=-4, text_dy=-0.2)
    arrow(ax, 50, 38, 50, 22, "لغو پزشک / بیمار", fs=8.2, text_dx=8)
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
