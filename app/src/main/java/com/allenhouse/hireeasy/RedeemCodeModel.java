package com.allenhouse.hireeasy;

public class RedeemCodeModel {
    private String redeemId;
    private String code;
    private String amount;
    private String usageTime;
    private String expireDate;
    private String createdAt;

    // Default constructor required for Firebase
    public RedeemCodeModel() {
    }

    public RedeemCodeModel(String redeemId, String code, String amount, String usageTime, String expireDate, String createdAt) {
        this.redeemId = redeemId;
        this.code = code;
        this.amount = amount;
        this.usageTime = usageTime;
        this.expireDate = expireDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getRedeemId() {
        return redeemId;
    }

    public void setRedeemId(String redeemId) {
        this.redeemId = redeemId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(String usageTime) {
        this.usageTime = usageTime;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}