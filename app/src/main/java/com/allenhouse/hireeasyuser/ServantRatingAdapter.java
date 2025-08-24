package com.allenhouse.hireeasyuser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServantRatingAdapter extends RecyclerView.Adapter<ServantRatingAdapter.RatingViewHolder> {

    private List<ServantRatingModel> ratingList;
    private String currentUserId;
    private Context context;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String ratingId);
    }

    public ServantRatingAdapter(List<ServantRatingModel> ratingList, String currentUserId, OnDeleteClickListener deleteClickListener) {
        this.ratingList = ratingList;
        this.currentUserId = currentUserId;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.ratings_review, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        ServantRatingModel model = ratingList.get(position);

        holder.tvReviewerName.setText(model.getUsername());
        holder.ratingBar.setRating(model.getRating());
        holder.tvReviewText.setText(model.getReview());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(model.getTimestamp()));
        holder.tvReviewTime.setText(formattedDate);

        // Show delete button only for current user's rating
        if (model.getUserId().equals(currentUserId)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(model.getRatingId()));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvReviewTime, tvReviewText;
        RatingBar ratingBar;
        ImageButton btnDelete;

        RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewTime = itemView.findViewById(R.id.tv_review_time);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}