package com.allenhouse.hireeasyuser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {
    private List<PaymentHistoryModel> paymentList;

    public PaymentHistoryAdapter(List<PaymentHistoryModel> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentHistoryModel payment = paymentList.get(position);
        holder.paymentId.setText("Transaction ID: " + payment.getPaymentId());
        holder.paymentEmail.setText("Email: " + payment.getEmail());
        holder.paymentAmount.setText("Amount: ₹" + payment.getAmount());
        holder.paymentDate.setText("Date: " + payment.getDate());
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView paymentId, paymentEmail, paymentAmount, paymentDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            paymentId = itemView.findViewById(R.id.paymentID);
            paymentEmail = itemView.findViewById(R.id.paymentEmail);
            paymentAmount = itemView.findViewById(R.id.paymentAmount);
            paymentDate = itemView.findViewById(R.id.paymentDate);
        }
    }
}