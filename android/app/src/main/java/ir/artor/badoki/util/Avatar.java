package ir.artor.badoki.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import ir.artor.badoki.R;

/** رنگ و حروف اول آواتار بر اساس نام */
public class Avatar {

    private static final int[] COLORS = {
            R.color.avatar_1, R.color.avatar_2, R.color.avatar_3, R.color.avatar_4,
            R.color.avatar_5, R.color.avatar_6, R.color.avatar_7, R.color.avatar_8
    };

    public static int colorFor(Context ctx, String name) {
        if (name == null || name.isEmpty()) return ctx.getColor(R.color.avatar_1);
        int hash = Math.abs(name.hashCode());
        return ctx.getColor(COLORS[hash % COLORS.length]);
    }

    public static ColorStateList tintFor(Context ctx, String name) {
        return ColorStateList.valueOf(colorFor(ctx, name));
    }

    /** حروف اول دو کلمه اول نام */
    public static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "؟";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0];
        String result = String.valueOf(first.charAt(0));
        if (parts.length > 1 && !parts[1].isEmpty()) {
            result += parts[1].charAt(0);
        }
        return result;
    }

    /** رنگ متن روی آواتار: سفید در حالت روشن، تیره در حالت شب */
    public static int textColor(Context ctx) {
        boolean night = (ctx.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        return night ? Color.rgb(6, 26, 24) : Color.WHITE;
    }
}
