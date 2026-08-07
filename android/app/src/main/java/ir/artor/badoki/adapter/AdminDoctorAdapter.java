package ir.artor.badoki.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.R;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** آداپتور مدیریت پزشکان (ادمین) — با دکمه‌های ویرایش و حذف */
public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.VH> {

    public interface Listener {
        void onEdit(Models.Doctor doctor);
        void onDelete(Models.Doctor doctor);
    }

    private final List<Models.Doctor> items = new ArrayList<>();
    private final Listener listener;

    public AdminDoctorAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Models.Doctor> doctors) {
        items.clear();
        if (doctors != null) items.addAll(doctors);
        notifyDataSetChanged();
    }

    /** جایگزینی خوش‌بینانه یک پزشک */
    public void replace(Models.Doctor updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == updated.id) {
                items.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
        items.add(0, updated);
        notifyItemInserted(0);
    }

    public void removeById(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_doctor, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Doctor d = items.get(position);
        holder.avatar.setText(Avatar.initials(d.fullName));
        holder.avatar.setBackgroundTintList(Avatar.tintFor(holder.itemView.getContext(), d.fullName));
        holder.avatar.setTextColor(Avatar.textColor(holder.itemView.getContext()));
        holder.name.setText(d.fullName);
        holder.specialty.setText(d.specialty);
        holder.meta.setText(d.city + (d.hospitalName == null || d.hospitalName.isEmpty()
                ? "" : " • " + d.hospitalName));
        holder.price.setText(Fmt.toman(d.visitPrice));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(d);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(d);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView avatar, name, specialty, meta, price;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.doctor_name);
            specialty = itemView.findViewById(R.id.doctor_specialty);
            meta = itemView.findViewById(R.id.doctor_meta);
            price = itemView.findViewById(R.id.doctor_price);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
