package ir.artor.badoki.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.R;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** آداپتور لیست پزشکان (لیست عمودی و لیست افقی داشبورد) */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.VH> {

    public interface Listener {
        void onClick(Models.Doctor doctor);
    }

    private final List<Models.Doctor> items = new ArrayList<>();
    private final boolean horizontal;
    private final Listener listener;

    public DoctorAdapter(boolean horizontal, Listener listener) {
        this.horizontal = horizontal;
        this.listener = listener;
    }

    public void submit(List<Models.Doctor> doctors) {
        items.clear();
        if (doctors != null) items.addAll(doctors);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                horizontal ? R.layout.item_doctor_horizontal : R.layout.item_doctor,
                parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Doctor d = items.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(d);
        });

        holder.avatar.setText(Avatar.initials(d.fullName));
        holder.avatar.setBackgroundTintList(Avatar.tintFor(holder.itemView.getContext(), d.fullName));
        holder.avatar.setTextColor(Avatar.textColor(holder.itemView.getContext()));

        holder.name.setText(d.fullName);
        holder.specialty.setText(d.specialty);

        holder.rating.setText(Fmt.rating(d.rating));
        if (holder.reviews != null) {
            holder.reviews.setText("(" + Fmt.fa(d.reviewCount) + " " +
                    holder.itemView.getContext().getString(R.string.reviews) + ")");
        }
        if (holder.meta != null) {
            String meta = d.city;
            if (d.hospitalName != null && !d.hospitalName.isEmpty()) {
                meta = meta + " • " + d.hospitalName;
            }
            holder.meta.setText(meta);
        }
        if (holder.price != null) {
            holder.price.setText(Fmt.toman(d.visitPrice));
        }
        if (holder.exp != null) {
            holder.exp.setText(Fmt.fa(d.experienceYears) + " " +
                    holder.itemView.getContext().getString(R.string.years_exp));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView avatar, name, specialty, rating, reviews, meta, price, exp;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.doctor_name);
            specialty = itemView.findViewById(R.id.doctor_specialty);
            rating = itemView.findViewById(R.id.doctor_rating);
            reviews = itemView.findViewById(R.id.doctor_reviews);
            meta = itemView.findViewById(R.id.doctor_meta);
            price = itemView.findViewById(R.id.doctor_price);
            exp = itemView.findViewById(R.id.doctor_exp);
        }
    }
}
