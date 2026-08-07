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

/** آداپتور لیست نوبت‌ها با اکشن‌های لغو، تغییر زمان و حذف */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface Listener {
        void onCancel(Models.Appointment appointment);
        void onReschedule(Models.Appointment appointment);
        void onDelete(Models.Appointment appointment);
    }

    private final List<Models.Appointment> items = new ArrayList<>();
    private final Listener listener;

    public AppointmentAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Models.Appointment> appointments) {
        items.clear();
        if (appointments != null) items.addAll(appointments);
        notifyDataSetChanged();
    }

    public List<Models.Appointment> items() {
        return items;
    }

    /** جایگزینی یک نوبت در لیست (برای به‌روزرسانی خوش‌بینانه) */
    public void replace(Models.Appointment updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == updated.id) {
                items.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /** حذف یک نوبت از لیست و برگرداندن ایندکس */
    public int removeById(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id) {
                items.remove(i);
                notifyItemRemoved(i);
                return i;
            }
        }
        return -1;
    }

    public void restoreAt(int index, Models.Appointment appointment) {
        items.add(Math.max(0, Math.min(index, items.size())), appointment);
        notifyItemInserted(Math.max(0, Math.min(index, items.size())));
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Appointment a = items.get(position);
        holder.itemView.setTag(a.id);

        holder.avatar.setText(Avatar.initials(a.doctorName));
        holder.avatar.setBackgroundTintList(Avatar.tintFor(holder.itemView.getContext(), a.doctorName));
        holder.avatar.setTextColor(Avatar.textColor(holder.itemView.getContext()));

        holder.name.setText(a.doctorName);
        holder.specialty.setText(a.specialty);

        holder.status.setText(statusLabel(holder.itemView, a.status));
        holder.status.setBackgroundTintList(ColorStateList.valueOf(statusBg(holder.itemView, a.status)));
        holder.status.setTextColor(statusText(holder.itemView, a.status));

        holder.datetime.setText(Fmt.dateFull(holder.itemView.getContext(), a.date)
                + "  •  " + Fmt.faTime(a.time));

        String hospital = (a.hospitalName == null || a.hospitalName.isEmpty())
                ? a.city : a.hospitalName + "، " + a.city;
        holder.hospital.setText(hospital);
        holder.price.setText(Fmt.toman(a.visitPrice));

        boolean active = "CONFIRMED".equals(a.status) || "PENDING".equals(a.status);
        boolean canceled = "CANCELED".equals(a.status);
        holder.btnReschedule.setVisibility(active ? View.VISIBLE : View.GONE);
        holder.btnCancel.setVisibility(active ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(canceled ? View.VISIBLE : View.GONE);

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(a);
        });
        holder.btnReschedule.setOnClickListener(v -> {
            if (listener != null) listener.onReschedule(a);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(a);
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
        TextView avatar, name, specialty, status, datetime, hospital, price;
        Button btnCancel, btnReschedule, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.appt_doctor_name);
            specialty = itemView.findViewById(R.id.appt_specialty);
            status = itemView.findViewById(R.id.appt_status);
            datetime = itemView.findViewById(R.id.appt_datetime);
            hospital = itemView.findViewById(R.id.appt_hospital);
            price = itemView.findViewById(R.id.appt_price);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnReschedule = itemView.findViewById(R.id.btn_reschedule);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
