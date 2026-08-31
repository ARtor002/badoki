package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** برگه تأیید رزرو نوبت */
public class BookSheet extends BottomSheetDialogFragment {

    private static final String ARG_DOCTOR = "doctor";
    private static final String ARG_DATE = "date";
    private static final String ARG_TIME = "time";

    /** فراخوانی نتیجه رزرو — به state فرگمنت وابسته نیست و همیشه کار می‌کند */
    public interface Listener {
        /** bookedAgain=true یعنی «رزرو نوبت دیگر»، false یعنی «مشاهده نوبت‌های من» */
        void onResult(boolean bookedAgain);
    }

    private Models.Doctor doctor;
    private String date;
    private String time;
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static BookSheet newInstance(Models.Doctor doctor, String date, String time) {
        BookSheet sheet = new BookSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DOCTOR, doctor);
        args.putString(ARG_DATE, date);
        args.putString(ARG_TIME, time);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_book, container, false);

        doctor = (Models.Doctor) getArguments().getSerializable(ARG_DOCTOR);
        date = getArguments().getString(ARG_DATE);
        time = getArguments().getString(ARG_TIME);

        TextView doctorName = root.findViewById(R.id.book_doctor_name);
        TextView doctorSpecialty = root.findViewById(R.id.book_doctor_specialty);
        TextView summary = root.findViewById(R.id.book_summary);
        TextInputEditText notesField = root.findViewById(R.id.book_notes);
        Button confirmBtn = root.findViewById(R.id.book_confirm);
        Button cancelBtn = root.findViewById(R.id.book_cancel);
        TextView errorText = root.findViewById(R.id.book_error);

        doctorName.setText(doctor.fullName);
        doctorSpecialty.setText(doctor.specialty);
        summary.setText(getString(R.string.book_summary,
                Fmt.dateFull(requireContext(), date), Fmt.faTime(time)));

        cancelBtn.setOnClickListener(v -> dismiss());

        confirmBtn.setOnClickListener(v -> {
            confirmBtn.setEnabled(false);
            confirmBtn.setText(R.string.loading);
            errorText.setVisibility(View.GONE);

            Models.AppointmentRequest request = new Models.AppointmentRequest();
            request.doctorId = doctor.id;
            request.date = date;
            request.time = time;
            String notes = notesField.getText() == null ? null : notesField.getText().toString().trim();
            request.notes = (notes == null || notes.isEmpty()) ? null : notes;

            BadokiApp.api().createAppointment(request).enqueue(new Callback<Models.Appointment>() {
                @Override
                public void onResponse(@NonNull Call<Models.Appointment> call,
                                       @NonNull Response<Models.Appointment> response) {
                    confirmBtn.setEnabled(true);
                    confirmBtn.setText(R.string.book_appointment);
                    if (response.isSuccessful() && response.body() != null) {
                        dismiss();
                        showSuccess(response.body());
                    } else {
                        errorText.setText(ApiClient.errorMessage(
                                new retrofit2.HttpException(response)));
                        errorText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Models.Appointment> call, @NonNull Throwable t) {
                    confirmBtn.setEnabled(true);
                    confirmBtn.setText(R.string.book_appointment);
                    if (ApiClient.isUnauthorized(t)) {
                        ApiClient.handleUnauthorized(requireContext());
                        dismiss();
                        return;
                    }
                    errorText.setText(ApiClient.errorMessage(t));
                    errorText.setVisibility(View.VISIBLE);
                }
            });
        });
        return root;
    }

    private void showSuccess(Models.Appointment appointment) {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.book_success_title)
                .setMessage(getString(R.string.book_success_sub,
                        Fmt.dateFull(requireContext(), appointment.date),
                        Fmt.faTime(appointment.time)))
                .setCancelable(false)
                .setPositiveButton(R.string.book_view_appointments, (d, w) -> {
                    d.dismiss();
                    if (listener != null) listener.onResult(false); // مشاهده نوبت‌های من
                })
                .setNegativeButton(R.string.book_another, (d, w) -> {
                    d.dismiss();
                    if (listener != null) listener.onResult(true);  // رزرو نوبت دیگر
                })
                .create();
        dialog.show();
    }
}
