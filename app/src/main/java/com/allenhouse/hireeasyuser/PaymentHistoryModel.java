package com.allenhouse.hireeasyuser;

public class PaymentHistoryModel {
    private String paymentId;
    private String email;
    private String amount;
    private String date;
    private String userId;

    public PaymentHistoryModel() {
        // Default constructor required for Firebase
    }

    public PaymentHistoryModel(String paymentId, String email, String amount, String date, String userId) {
        this.paymentId = paymentId;
        this.email = email;
        this.amount = amount;
        this.date = date;
        this.userId = userId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getEmail() {
        return email;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }
}