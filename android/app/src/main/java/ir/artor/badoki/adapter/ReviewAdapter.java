package ir.artor.badoki.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.R;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.Jalali;
import ir.artor.badoki.util.StarRow;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** آداپتور لیست نظرات بیماران درباره یک پزشک */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    public interface Listener {
        void onDelete(Models.Review review);
    }

    private final List<Models.Review> items = new ArrayList<>();
    private final Listener listener;
    private final long myUserId;

    public ReviewAdapter(long myUserId, Listener listener) {
        this.myUserId = myUserId;
        this.listener = listener;
    }

    public void submit(List<Models.Review> reviews) {
        items.clear();
        if (reviews != null) items.addAll(reviews);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Review r = items.get(position);
        holder.patientName.setText(r.patientName);
        StarRow.populate(holder.itemView.getContext(), holder.stars, r.rating);
        holder.time.setText(formatTime(r.createdAt));
        if (r.comment != null && !r.comment.isEmpty()) {
            holder.comment.setText(r.comment);
            holder.comment.setVisibility(View.VISIBLE);
        } else {
            holder.comment.setVisibility(View.GONE);
        }
        // دکمه حذف فقط برای نظر خود کاربر
        boolean mine = r.patientId == myUserId;
        holder.deleteBtn.setVisibility(mine ? View.VISIBLE : View.GONE);
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(r);
        });
    }

    private String formatTime(String iso) {
        try {
            LocalDateTime dt = LocalDateTime.ofInstant(
                    Instant.parse(iso), ZoneId.systemDefault());
            Jalali.JDate j = Jalali.toJalali(dt.toLocalDate());
            String date = String.format("%04d/%02d/%02d", j.jy, j.jm, j.jd);
            return Fmt.fa(date);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView patientName, time, comment, deleteBtn;
        LinearLayout stars;

        VH(@NonNull View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.review_patient);
            stars = itemView.findViewById(R.id.review_stars);
            time = itemView.findViewById(R.id.review_time);
            comment = itemView.findViewById(R.id.review_comment);
            deleteBtn = itemView.findViewById(R.id.review_delete);
        }
    }
}
