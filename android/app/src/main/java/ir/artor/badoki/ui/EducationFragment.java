package ir.artor.badoki.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ir.artor.badoki.R;

/**
 * بخش آموزش — راهنمای گام‌به‌گام، سوالات متداول و نکات امنیتی
 * هر کارت با کلیک باز/بسته می‌شود
 */
public class EducationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_education, container, false);
        LinearLayout containerView = root.findViewById(R.id.edu_container);

        addSection(containerView, "📖 رزرو نوبت در ۵ قدم",
                "۱. وارد حساب کاربری خود شوید (یا ثبت‌نام کنید).\n"
                        + "۲. از بخش «پزشکان»، پزشک موردنظر را با جستجو یا فیلتر تخصص/شهر پیدا کنید.\n"
                        + "۳. روی پزشک بزنید و اطلاعات، هزینه و روزهای ویزیت او را ببینید.\n"
                        + "۴. از روزهای کاری، تاریخ و سپس یکی از ساعت‌های خالی را انتخاب کنید.\n"
                        + "۵. دکمه «رزرو نوبت» را بزنید؛ نوبت شما با وضعیت «در انتظار تأیید پزشک» ثبت می‌شود.");

        addSection(containerView, "🩺 راهنمای بیماران",
                "• بعد از رزرو، پزشک باید نوبت را تأیید کند؛ از بخش «اطلاع‌رسانی» از نتیجه باخبر می‌شوید.\n"
                        + "• برای تغییر زمان نوبت، در «نوبت‌های من» گزینه «تغییر زمان» را بزنید و ساعت خالی جدید را انتخاب کنید.\n"
                        + "• اگر نمی‌توانید حضور یابید، حتماً نوبت را لغو کنید تا پزشک زمانش خالی بماند.\n"
                        + "• نوبت‌های تأییدنشده که تاریخشان بگذرد، به‌صورت خودکار «منقضی» می‌شوند.\n"
                        + "• هزینه ویزیت هنگام رزرو نمایش داده می‌شود و پرداخت در محل انجام می‌گیرد.");

        addSection(containerView, "👨‍⚕️ راهنمای پزشکان",
                "• در بخش «نوبت‌های بیماران»، درخواست‌های جدید با وضعیت «در انتظار» دیده می‌شوند.\n"
                        + "• با دکمه «تأیید نوبت»، نوبت قطعی می‌شود و بیمار اعلان می‌گیرد.\n"
                        + "• بعد از ویزیت، «انجام شد» را بزنید تا نوبت در تاریخچه بسته شود.\n"
                        + "• اگر قادر به پذیرش بیمار نیستید، «لغو نوبت» را بزنید تا بیمار مطلع شود.\n"
                        + "• از «پروفایل پزشک» می‌توانید هزینه، ساعات کاری و روزهای ویزیت را تغییر دهید.");

        addSection(containerView, "👑 راهنمای مدیر سیستم",
                "• «مدیریت پزشکان»: افزودن پزشک جدید (با ساخت خودکار حساب پزشک)، ویرایش و حذف.\n"
                        + "• «مدیریت کاربران»: مشاهده همه کاربران و نقش‌های آن‌ها.\n"
                        + "• داشبورد ادمین آمار کل سیستم (پزشکان، بیماران، نوبت‌ها و در انتظارها) را نشان می‌دهد.");

        addSection(containerView, "❓ سوالات متداول",
                "س: چرا نوبت من «در انتظار تأیید» است؟\n"
                        + "ج: همه نوبت‌ها ابتدا باید توسط پزشک تأیید شوند؛ نتیجه در بخش «اطلاع‌رسانی» اعلام می‌شود.\n\n"
                        + "س: اگر پزشک نوبتم را لغو کند چه می‌شود؟\n"
                        + "ج: یک اعلان فوری برای شما ارسال می‌شود و می‌توانید زمان دیگری رزرو کنید.\n\n"
                        + "س: چرا بعضی ساعت‌ها غیرفعال هستند؟\n"
                        + "ج: آن اسلات‌ها قبلاً رزرو شده‌اند یا خارج از ساعات کاری پزشک هستند.\n\n"
                        + "س: تا چه زمانی می‌توانم رزرو کنم؟\n"
                        + "ج: تا ۳۰ روز آینده؛ رزرو برای تاریخ‌های گذشته ممکن نیست.\n\n"
                        + "س: نوبت من به پایان رسیده ولی «انجام‌شده» نیست؟\n"
                        + "ج: نوبت‌های قطعی بعد از گذشتن تاریخشان به‌صورت خودکار «انجام‌شده» می‌شوند.");

        addSection(containerView, "🔐 نکات امنیتی",
                "• رمز عبور خود را با کسی در میان نگذارید و حداقل ۶ کاراکتر انتخاب کنید.\n"
                        + "• کد تأیید (OTP) را فقط خودتان استفاده کنید؛ هیچ‌کس از سامانه این کد را نمی‌خواهد.\n"
                        + "• بعد از کار با دستگاه‌های مشترک، حتماً از حساب خارج شوید.\n"
                        + "• برای ورود از تأیید دومرحله‌ای استفاده کنید؛ در صورت مفقودی گوشی، از طریق «فراموشی رمز» حساب را بازیابی کنید.");

        return root;
    }

    private void addSection(LinearLayout container, String title, String body) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_education, container, false);
        TextView titleTv = card.findViewById(R.id.edu_title);
        TextView bodyTv = card.findViewById(R.id.edu_body);
        titleTv.setText(title);
        bodyTv.setText(body);
        bodyTv.setVisibility(View.GONE);

        // پس‌زمینه گرد کارت
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ContextCompat.getColor(requireContext(), R.color.surface));
        bg.setCornerRadius(20f);
        card.setBackground(bg);

        titleTv.setOnClickListener(v -> {
            boolean show = bodyTv.getVisibility() != View.VISIBLE;
            bodyTv.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        container.addView(card);
    }
}
