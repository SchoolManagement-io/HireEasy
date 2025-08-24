package com.allenhouse.hireeasyuser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ServantCardAdapter extends RecyclerView.Adapter<ServantCardAdapter.ServantViewHolder> {

    private final List<ServantCardModel> servantList;
    private final Context context;
    private final OnViewProfileClickListener listener;

    public interface OnViewProfileClickListener {
        void onViewProfileClick(String servantId, String servantName); // ✅ new
    }

    public ServantCardAdapter(List<ServantCardModel> servantList, Context context, OnViewProfileClickListener listener) {
        this.servantList = servantList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.servant_list, parent, false);
        return new ServantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServantViewHolder holder, int position) {
        ServantCardModel servant = servantList.get(position);
        holder.servantName.setText(servant.getName());
        holder.servantCategory.setText(servant.getCategory());
        holder.servantExperience.setText(servant.getExperience() + " yrs experience");
        holder.servantArea.setText(servant.getArea());
        if (servant.getUrgentCharge() != null && !servant.getUrgentCharge().isEmpty() && !servant.getUrgentCharge().equalsIgnoreCase("Not Available")) {
            holder.urgentPrice.setText("₹" + servant.getUrgentCharge() + " /hr");
            holder.urgentPrice.setVisibility(View.VISIBLE);
            holder.expectedSalary.setVisibility(View.GONE);
        } else {
            holder.expectedSalary.setText("₹" + servant.getExpectedSalary() + " /month");
            holder.urgentPrice.setVisibility(View.GONE);
            holder.expectedSalary.setVisibility(View.VISIBLE);
        }
        holder.ratingAvg.setText(String.format("%.1f", servant.getAvgRating()));
        holder.availability.setText("Yes".equalsIgnoreCase(servant.getAvailability()) ? "Available" : "Busy");
        holder.availability.setTextColor(context.getResources().getColor(
                "Yes".equalsIgnoreCase(servant.getAvailability()) ? R.color.colorSuccess : R.color.statusBusy
        ));
        holder.verifiedText.setVisibility(servant.isVerified() ? View.VISIBLE : View.GONE);
        holder.verifiedIcon.setVisibility(servant.isVerified() ? View.VISIBLE : View.GONE);
        // Use Glide to load profile photo, fallback to default_profile if URL is null or empty
        if (servant.getProfilePhoto() != null && !servant.getProfilePhoto().isEmpty()) {
            Glide.with(context).load(servant.getProfilePhoto()).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.servantProfile);
        } else {
            holder.servantProfile.setImageResource(R.drawable.default_profile);
        }
        holder.viewProfileBtn.setOnClickListener(v -> listener.onViewProfileClick(servant.getServantId(), servant.getName()));
    }

    @Override
    public int getItemCount() {
        return servantList.size();
    }

    static class ServantViewHolder extends RecyclerView.ViewHolder {
        ImageView servantProfile, verifiedIcon;
        TextView servantName, servantCategory, servantExperience, servantArea, urgentPrice, expectedSalary, ratingAvg, availability, verifiedText;
        Button viewProfileBtn;

        ServantViewHolder(@NonNull View itemView) {
            super(itemView);
            servantProfile = itemView.findViewById(R.id.servant_profile);
            verifiedIcon = itemView.findViewById(R.id.verifiedIcon);
            servantName = itemView.findViewById(R.id.servant_name);
            servantCategory = itemView.findViewById(R.id.servant_category);
            servantExperience = itemView.findViewById(R.id.servant_experience);
            servantArea = itemView.findViewById(R.id.servant_area);
            urgentPrice = itemView.findViewById(R.id.urgent_price);
            expectedSalary = itemView.findViewById(R.id.expected_salary);
            ratingAvg = itemView.findViewById(R.id.rating_avg);
            availability = itemView.findViewById(R.id.availability);
            verifiedText = itemView.findViewById(R.id.verifiedText);
            viewProfileBtn = itemView.findViewById(R.id.viewProfileBtn);
        }
    }
}