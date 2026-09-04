package ir.artor.badoki.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.R;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** آداپتور نوبت‌های بیماران برای پزشک — با اکشن‌های تأیید، انجام‌شده و لغو */
public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.VH> {

    public interface Listener {
        void onConfirm(Models.Appointment appointment);
        void onComplete(Models.Appointment appointment);
        void onCancel(Models.Appointment appointment);
    }

    private final List<Models.Appointment> items = new ArrayList<>();
    private final Listener listener;

    public DoctorAppointmentAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Models.Appointment> appointments) {
        items.clear();
        if (appointments != null) items.addAll(appointments);
        notifyDataSetChanged();
    }

    public void replace(Models.Appointment updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == updated.id) {
                items.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_appointment, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Appointment a = items.get(position);

        holder.avatar.setText(Avatar.initials(a.patientName == null ? "؟" : a.patientName));
        holder.avatar.setBackgroundTintList(
                Avatar.tintFor(holder.itemView.getContext(), a.patientName));
        holder.avatar.setTextColor(Avatar.textColor(holder.itemView.getContext()));

        holder.patientName.setText(a.patientName == null ? "—" : a.patientName);
        if (a.patientPhone != null && !a.patientPhone.isEmpty()) {
            holder.patientPhone.setText(Fmt.fa(a.patientPhone));
            holder.patientPhone.setVisibility(View.VISIBLE);
        } else {
            holder.patientPhone.setVisibility(View.GONE);
        }

        holder.status.setText(statusLabel(holder.itemView, a.status));
        android.content.Context ctx = holder.itemView.getContext();
        holder.status.setBackgroundTintList(ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(ctx, statusBg(holder.itemView, a.status))));
        holder.status.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, statusText(holder.itemView, a.status)));

        holder.datetime.setText(Fmt.dateFull(holder.itemView.getContext(), a.date)
                + "  •  " + Fmt.faTime(a.time));
        String detail = a.specialty;
        if (a.notes != null && !a.notes.isEmpty()) {
            detail += " — " + a.notes;
        }
        holder.detail.setText(detail);

        boolean pending = "PENDING".equals(a.status);
        boolean confirmed = "CONFIRMED".equals(a.status);
        // دکمه «انجام شد» فقط برای نوبت‌هایی که زمانشان رسیده یا گذشته مجاز است
        boolean canComplete = confirmed && !java.time.LocalDate.parse(a.date).isAfter(java.time.LocalDate.now());
        holder.btnConfirm.setVisibility(pending ? View.VISIBLE : View.GONE);
        holder.btnComplete.setVisibility(canComplete ? View.VISIBLE : View.GONE);
        holder.btnCancel.setVisibility((pending || confirmed) ? View.VISIBLE : View.GONE);

        holder.btnConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onConfirm(a);
        });
        holder.btnComplete.setOnClickListener(v -> {
            if (listener != null) listener.onComplete(a);
        });
        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(a);
        });
    }

    private String statusLabel(View v, String status) {
        int res;
        switch (status == null ? "" : status) {
            case "CONFIRMED": res = R.string.status_CONFIRMED; break;
            case "PENDING": res = R.string.status_PENDING; break;
            case "COMPLETED": res = R.string.status_COMPLETED; break;
            case "CANCELED": res = R.string.status_CANCELED; break;
            default: res = R.string.status_PENDING;
        }
        return v.getContext().getString(res);
    }

    private int statusBg(View v, String status) {
        switch (status == null ? "" : status) {
            case "CONFIRMED": return R.color.statusConfirmedBg;
            case "COMPLETED": return R.color.statusCompletedBg;
            case "CANCELED": return R.color.statusCanceledBg;
            default: return R.color.statusPendingBg;
        }
    }

    private int statusText(View v, String status) {
        switch (status == null ? "" : status) {
            case "CONFIRMED": return R.color.statusConfirmedText;
            case "COMPLETED": return R.color.statusCompletedText;
            case "CANCELED": return R.color.statusCanceledText;
            default: return R.color.statusPendingText;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView avatar, patientName, patientPhone, status, datetime, detail;
        Button btnConfirm, btnComplete, btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            patientName = itemView.findViewById(R.id.patient_name);
            patientPhone = itemView.findViewById(R.id.patient_phone);
            status = itemView.findViewById(R.id.appt_status);
            datetime = itemView.findViewById(R.id.appt_datetime);
            detail = itemView.findViewById(R.id.appt_detail);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnComplete = itemView.findViewById(R.id.btn_complete);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
}
