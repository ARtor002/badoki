package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** برگه ثبت نظر و امتیاز برای پزشک — انتخاب ستاره + متن نظر */
public class ReviewSheet extends BottomSheetDialogFragment {

    private static final String ARG_DOCTOR_ID = "doctor_id";

    public interface Listener {
        void onReviewSubmitted();
    }

    private long doctorId;
    private int selectedStars = 0;
    private Listener listener;

    public static ReviewSheet newInstance(long doctorId) {
        ReviewSheet sheet = new ReviewSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCTOR_ID, doctorId);
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_review, container, false);
        doctorId = getArguments().getLong(ARG_DOCTOR_ID);

        LinearLayout starRow = root.findViewById(R.id.review_star_select);
        TextInputEditText commentField = root.findViewById(R.id.review_comment);
        Button submitBtn = root.findViewById(R.id.review_submit);
        TextView errorText = root.findViewById(R.id.review_error);

        // ساخت ۵ ستاره قابل انتخاب
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            ImageView iv = new ImageView(requireContext());
            iv.setImageResource(R.drawable.ic_star);
            int size = (int) (28 * getResources().getDisplayMetrics().density + 0.5f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density + 0.5f));
            iv.setLayoutParams(lp);
            iv.setColorFilter(getColorForStar(0));
            iv.setOnClickListener(v -> {
                selectedStars = star;
                for (int j = 0; j < starRow.getChildCount(); j++) {
                    ImageView child = (ImageView) starRow.getChildAt(j);
                    child.setColorFilter(getColorForStar(j + 1));
                }
            });
            starRow.addView(iv);
        }

        root.findViewById(R.id.review_cancel).setOnClickListener(v -> dismiss());

        submitBtn.setOnClickListener(v -> {
            if (selectedStars == 0) {
                errorText.setText(R.string.reviews_error_rating);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            submitBtn.setEnabled(false);
            Models.ReviewRequest req = new Models.ReviewRequest();
            req.doctorId = doctorId;
            req.rating = selectedStars;
            String comment = commentField.getText() == null ? null : commentField.getText().toString().trim();
            req.comment = (comment == null || comment.isEmpty()) ? null : comment;

            BadokiApp.api().createReview(req).enqueue(new Callback<Models.Review>() {
                @Override
                public void onResponse(@NonNull Call<Models.Review> call,
                                       @NonNull Response<Models.Review> response) {
                    submitBtn.setEnabled(true);
                    if (response.isSuccessful()) {
                        dismiss();
                        if (listener != null) listener.onReviewSubmitted();
                    } else {
                        errorText.setText(ApiClient.errorMessage(new retrofit2.HttpException(response)));
                        errorText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Models.Review> call, @NonNull Throwable t) {
                    submitBtn.setEnabled(true);
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

    private int getColorForStar(int star) {
        return star <= selectedStars
                ? getResources().getColor(R.color.tertiary, null)
                : getResources().getColor(R.color.outline, null);
    }
}
