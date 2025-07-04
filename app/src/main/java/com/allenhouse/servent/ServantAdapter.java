
package com.allenhouse.servent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServantAdapter extends RecyclerView.Adapter<ServantAdapter.ServantViewHolder> {

    private List<ServantModel> servantList;
    private OnItemActionListener listener;
    private static final String TAG = "ServantAdapter";

    public ServantAdapter(List<ServantModel> servantList, OnItemActionListener listener) {
        this.servantList = servantList;
        this.listener = listener;
        Log.d(TAG, "ServantAdapter initialized with list size: " + (servantList != null ? servantList.size() : 0));
    }

    public interface OnItemActionListener {
        void onItemClick(int position);
        void onEditClick(ServantModel servant);
        void onDeleteClick(ServantModel servant);
    }

    public void updateList(List<ServantModel> newList) {
        this.servantList = newList;
        Log.d(TAG, "updateList called with new list size: " + (newList != null ? newList.size() : 0));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servant, parent, false);
        Log.d(TAG, "onCreateViewHolder: View inflated");
        return new ServantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServantViewHolder holder, int position) {
        ServantModel model = servantList.get(position);
        if (model == null) {
            Log.e(TAG, "onBindViewHolder: Model is null at position " + position);
            return;
        }

        if (holder.tvName == null || holder.tvMobile == null || holder.tvCurrentAddress == null ||
                holder.tvVerifiedStatus == null || holder.btnEdit == null || holder.btnDelete == null) {
            Log.e(TAG, "onBindViewHolder: One or more views are null at position " + position);
            return;
        }

        holder.tvName.setText(model.getName() != null ? model.getName() : "");
        holder.tvMobile.setText(model.getMobile() != null ? model.getMobile() : "");
        holder.tvCurrentAddress.setText(model.getCurrentAddress() != null ? model.getCurrentAddress() : "");

        if ("Verified".equalsIgnoreCase(model.getVerified())) {
            holder.tvVerifiedStatus.setText("Verified");
            holder.tvVerifiedStatus.setTextColor(0xFFFFD700); // Yellow
        } else {
            holder.tvVerifiedStatus.setText("Unverified");
            holder.tvVerifiedStatus.setTextColor(0xFFFF4444); // Red
        }

        // Buttons are always visible, no swipe needed
        holder.btnEdit.setVisibility(View.VISIBLE);
        holder.btnDelete.setVisibility(View.VISIBLE);

        holder.btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked for position: " + position);
            if (listener != null) {
                listener.onEditClick(model);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete button clicked for position: " + position);
            if (listener != null) {
                listener.onDeleteClick(model);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Item clicked at position: " + holder.getAdapterPosition());
                listener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return servantList != null ? servantList.size() : 0;
    }

    static class ServantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMobile, tvCurrentAddress, tvVerifiedStatus;
        Button btnEdit, btnDelete;

        public ServantViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                tvName = itemView.findViewById(R.id.tvName);
                tvMobile = itemView.findViewById(R.id.tvMobile);
                tvCurrentAddress = itemView.findViewById(R.id.tvAddress);
                tvVerifiedStatus = itemView.findViewById(R.id.tvVerifiedStatus);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);

                Log.d(TAG, "ServantViewHolder: tvName=" + (tvName != null ? "Initialized" : "Null"));
                Log.d(TAG, "ServantViewHolder: tvMobile=" + (tvMobile != null ? "Initialized" : "Null"));
                Log.d(TAG, "ServantViewHolder: tvCurrentAddress=" + (tvCurrentAddress != null ? "Initialized" : "Null"));
                Log.d(TAG, "ServantViewHolder: tvVerifiedStatus=" + (tvVerifiedStatus != null ? "Initialized" : "Null"));
                Log.d(TAG, "ServantViewHolder: btnEdit=" + (btnEdit != null ? "Initialized" : "Null"));
                Log.d(TAG, "ServantViewHolder: btnDelete=" + (btnDelete != null ? "Initialized" : "Null"));

                if (tvName == null || tvMobile == null || tvCurrentAddress == null ||
                        tvVerifiedStatus == null || btnEdit == null || btnDelete == null) {
                    Log.e(TAG, "ServantViewHolder: One or more views are null. Check item_servant.xml layout IDs.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ServantViewHolder", e);
            }
        }
    }
}
