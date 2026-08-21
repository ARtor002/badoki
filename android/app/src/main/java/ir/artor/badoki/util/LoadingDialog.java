package ir.artor.badoki.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import ir.artor.badoki.R;

/**
 * دیالوگ بارگذاری ساده و غیرقابل‌انصراف برای عملیات‌های شبکه‌ای کند
 * (مثل ارسال ایمیل کد تأیید که چند ثانیه طول می‌کشد)
 * تا کاربر فکر نکند برنامه قفل شده است.
 */
public class LoadingDialog {

    private Dialog dialog;

    /** نمایش دیالوگ با پیام دلخواه */
    public void show(Context context, String message) {
        dismiss();
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.dialog_loading);
        TextView msg = dialog.findViewById(R.id.loading_msg);
        msg.setText(message);
        dialog.show();
    }

    /** بستن دیالوگ (امن: اگر بسته یا null باشد خطا نمی‌دهد) */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {
            }
        }
        dialog = null;
    }
}
