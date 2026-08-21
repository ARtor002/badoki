package ir.artor.badoki.util;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import ir.artor.badoki.R;

/** ساخت ردیف ستاره‌های امتیاز (۵ ستاره) */
public class StarRow {

    /** پر کردن یک ردیف ستاره با امتیاز ۰ تا ۵ */
    public static void populate(Context ctx, LinearLayout container, int stars) {
        container.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            ImageView iv = new ImageView(ctx);
            iv.setImageResource(R.drawable.ic_star);
            int color = i <= stars
                    ? ContextCompat.getColor(ctx, R.color.tertiary)
                    : ContextCompat.getColor(ctx, R.color.outline);
            iv.setColorFilter(color);
            int size = dp(ctx, 18);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(ctx, 2));
            container.addView(iv, lp);
        }
    }

    /** نمایش امتیاز اعشاری (مثل ۴٫۹) با گردکردن به نزدیک‌ترین عدد صحیح */
    public static void populateForRating(Context ctx, LinearLayout container, double rating) {
        populate(ctx, container, (int) Math.round(rating));
    }

    private static int dp(Context ctx, float value) {
        return (int) (value * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
