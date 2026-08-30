package ir.artor.badoki.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.R;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** آداپتور مرکز اطلاع‌رسانی */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface Listener {
        void onClick(Models.Notification notification);
    }

    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final List<Models.Notification> items = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Models.Notification> notifications) {
        items.clear();
        if (notifications != null) items.addAll(notifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Notification n = items.get(position);
        holder.title.setText(n.title);
        holder.message.setText(formatMessage(n.message));
        holder.time.setText(Fmt.dateTimeJalali(n.createdAt));

        // آیکون و رنگ بر اساس نوع
        int iconRes, colorRes;
        switch (n.type == null ? "SYSTEM" : n.type) {
            case "APPOINTMENT_CANCELED":
                iconRes = R.drawable.ic_close;
                colorRes = R.color.statusCanceledText;
                break;
            case "APPOINTMENT_CONFIRMED":
                iconRes = R.drawable.ic_check;
                colorRes = R.color.statusConfirmedText;
                break;
            case "APPOINTMENT_COMPLETED":
                iconRes = R.drawable.ic_check;
                colorRes = R.color.statusCompletedText;
                break;
            case "APPOINTMENT_EXPIRED":
                iconRes = R.drawable.ic_clock;
                colorRes = R.color.statusPendingText;
                break;
            default:
                iconRes = R.drawable.ic_info;
                colorRes = R.color.onSurfaceVariant;
        }
        holder.icon.setBackgroundTintList(ColorStateList.valueOf(
                holder.itemView.getContext().getColor(colorRes)));
        holder.icon.setCompoundDrawablesWithIntrinsicBounds(
                iconRes, 0, 0, 0);

        // ناخوانده: پس‌زمینه روشن + نقطه آبی
        holder.itemView.setBackgroundResource(n.read
                ? android.R.color.transparent
                : R.drawable.bg_unread_notification);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(n);
        });
    }

    /** تاریخ میلادی داخل متن اعلان‌های قدیمی را به شمسی برمی‌گرداند */
    private String formatMessage(String msg) {
        if (msg == null || msg.isEmpty()) return "";
        Matcher m = ISO_DATE.matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            LocalDate g = Fmt.parseIso(m.group());
            String jalali = g == null ? m.group() : Fmt.dateNumeric(g);
            m.appendReplacement(sb, Matcher.quoteReplacement(jalali));
        }
        m.appendTail(sb);
        return Fmt.faDigits(sb.toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView icon, title, message, time;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notif_icon);
            title = itemView.findViewById(R.id.notif_title);
            message = itemView.findViewById(R.id.notif_message);
            time = itemView.findViewById(R.id.notif_time);
        }
    }
}
