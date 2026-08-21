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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** آداپتور مرکز اطلاع‌رسانی */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface Listener {
        void onClick(Models.Notification notification);
    }

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
        holder.message.setText(n.message);
        holder.time.setText(formatTime(n.createdAt));

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

    private String formatTime(String iso) {
        try {
            LocalDateTime dt = LocalDateTime.ofInstant(
                    Instant.parse(iso), ZoneId.systemDefault());
            return Fmt.fa(dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        } catch (Exception e) {
            return "";
        }
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
