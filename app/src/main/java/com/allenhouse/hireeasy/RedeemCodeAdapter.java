package com.allenhouse.hireeasy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RedeemCodeAdapter extends RecyclerView.Adapter<RedeemCodeAdapter.RedeemCodeViewHolder> {

    private List<RedeemCodeModel> redeemCodeList;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public RedeemCodeAdapter(List<RedeemCodeModel> redeemCodeList, OnDeleteClickListener onDeleteClickListener) {
        this.redeemCodeList = redeemCodeList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public RedeemCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.redeem_code_list, parent, false);
        return new RedeemCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemCodeViewHolder holder, int position) {
        RedeemCodeModel model = redeemCodeList.get(position);
        holder.tvCode.setText(model.getCode());
        holder.tvAmount.setText(model.getAmount() + " INR");
        holder.tvRemainingUsage.setText(model.getUsageTime());
        holder.tvExpireDate.setText(model.getExpireDate());
        holder.deleteRedeem.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return redeemCodeList.size();
    }

    public static class RedeemCodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvAmount, tvRemainingUsage, tvExpireDate;
        ImageButton deleteRedeem;

        public RedeemCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvRemainingUsage = itemView.findViewById(R.id.tvRemainingUsage);
            tvExpireDate = itemView.findViewById(R.id.tvExpireDate);
            deleteRedeem = itemView.findViewById(R.id.deleteRedeem);
        }
    }
}