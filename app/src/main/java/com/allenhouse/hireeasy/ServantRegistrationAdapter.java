package com.allenhouse.hireeasy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ServantRegistrationAdapter extends RecyclerView.Adapter<ServantRegistrationAdapter.ServantViewHolder> {

    private final List<ServantRegistrationModel> servantList;
    private final OnServantEditListener editListener;
    private final OnServantDeleteListener deleteListener;

    public interface OnServantEditListener {
        void onEdit(ServantRegistrationModel model, int position);
    }

    public interface OnServantDeleteListener {
        void onDelete(int position);
    }

    public ServantRegistrationAdapter(List<ServantRegistrationModel> servantList,
                                      OnServantEditListener editListener,
                                      OnServantDeleteListener deleteListener) {
        this.servantList = servantList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ServantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.servant_list, parent, false);
        return new ServantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServantViewHolder holder, int position) {
        ServantRegistrationModel model = servantList.get(position);
        holder.tvServantID.setText(model.getId());
        holder.tvName.setText(model.getName());
        holder.tvMobile.setText(model.getMobile());
        holder.tvAddress.setText(model.getCurrentAddress());
        holder.tvVerifiedStatus.setVisibility(model.getIsVerified() ? View.VISIBLE : View.GONE);

        // Load profile image with Glide
        if (model.getProfilePhoto() != null && !model.getProfilePhoto().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(model.getProfilePhoto())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.tvProfileImage);
        } else {
            holder.tvProfileImage.setImageResource(R.drawable.default_profile);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(model, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return servantList.size();
    }

    public static class ServantViewHolder extends RecyclerView.ViewHolder {
        TextView tvServantID, tvName, tvMobile, tvAddress;
        ImageView tvProfileImage, tvVerifiedStatus;
        ImageButton btnEdit, btnDelete;

        public ServantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServantID = itemView.findViewById(R.id.tvServantID);
            tvName = itemView.findViewById(R.id.tvName);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvProfileImage = itemView.findViewById(R.id.tvProfileImage);
            tvVerifiedStatus = itemView.findViewById(R.id.tvVerifiedStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}