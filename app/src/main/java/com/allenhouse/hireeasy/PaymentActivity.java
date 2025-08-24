package com.allenhouse.hireeasy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PaymentActivity extends BaseActivity implements PaymentResultListener {
    private EditText editRedeem;
    private TextView payableAmount, agentEmail, agentMobile, appliedSuccessText;
    private ImageButton appliedSuccessBtn;
    private Button btnApply, payAmountBtn;
    private String agentId, agentEmailStr, agentMobileStr, servantId, redeemCode = "";
    private double originalAmount = 50.0;
    private double payableAmountValue = 50.0;
    private DatabaseReference databaseReference;
    private boolean isRedeemApplied = false;
    private ServantRegistrationModel servantModel;

    public static final String EXTRA_AGENT_ID = "agent_id";
    public static final String EXTRA_SERVANT_MODEL = "servant_model";
    public static final String EXTRA_PAYMENT_AMOUNT = "payment_amount";
    public static final String EXTRA_REDEEM_CODE = "redeem_code";
    public static final String EXTRA_AGENT_EMAIL = "agent_email";
    public static final String EXTRA_PAYMENT_ID = "payment_id";
    ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_dialog);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        agentId = intent.getStringExtra(EXTRA_AGENT_ID);
        servantModel = intent.getParcelableExtra(EXTRA_SERVANT_MODEL);
        if (servantModel == null) {
            Toast.makeText(this, "Error: Servant data not received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        servantId = servantModel.getId().isEmpty() ? generateServantId() : servantModel.getId();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("redeem_codes");

        editRedeem = findViewById(R.id.editRedeem);
        payableAmount = findViewById(R.id.payableAmount);
        agentEmail = findViewById(R.id.agentEmail);
        agentMobile = findViewById(R.id.agentMobile);
        appliedSuccessBtn = findViewById(R.id.appliedSuccessBtn);
        appliedSuccessText = findViewById(R.id.appliedSuccessText);
        btnApply = findViewById(R.id.btnApply);
        payAmountBtn = findViewById(R.id.payAmountBtn);

        loadAgentDetails();

        payableAmount.setText(String.format("Rs. %.2f", payableAmountValue));

        btnApply.setOnClickListener(v -> applyRedeemCode());
        payAmountBtn.setOnClickListener(v -> handlePayment());
    }

    private void loadAgentDetails() {
        DatabaseReference agentRef = FirebaseDatabase.getInstance().getReference().child("agents").child(agentId);
        agentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    agentEmailStr = snapshot.child("email").getValue(String.class);
                    agentMobileStr = snapshot.child("mobile_number").getValue(String.class);
                    agentEmail.setText(agentEmailStr);
                    agentMobile.setText(agentMobileStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Failed to load agent details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRedeemCode() {
        if (isRedeemApplied) {
            Toast.makeText(this, "Only one redeem code can be applied at a time", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = editRedeem.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter a redeem code", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                boolean found = false;

                for (DataSnapshot codeSnap : snapshot.getChildren()) {
                    String dbCode = codeSnap.child("code").getValue(String.class);
                    if (dbCode != null && dbCode.equalsIgnoreCase(code)) {
                        found = true;

                        String amountStr = codeSnap.child("amount").getValue(String.class);
                        String expireDate = codeSnap.child("expire_date").getValue(String.class);
                        String usageTime = codeSnap.child("usage_time").getValue(String.class);

                        if (amountStr != null && expireDate != null && usageTime != null) {
                            try {
                                double discount = Double.parseDouble(amountStr);

                                boolean isUnlimitedUsage = usageTime.equalsIgnoreCase("Unlimited");
                                boolean isNoExpiry = expireDate.equalsIgnoreCase("No Expiry Date");

                                if (!isNoExpiry) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date expireDateObj = sdf.parse(expireDate);
                                    Date currentDate = new Date();
                                    if (expireDateObj != null && currentDate.after(expireDateObj)) {
                                        Toast.makeText(PaymentActivity.this, "Redeem Code Expired", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                int usageLeft = -1;
                                if (!isUnlimitedUsage) {
                                    usageLeft = Integer.parseInt(usageTime);
                                    if (usageLeft <= 0) {
                                        Toast.makeText(PaymentActivity.this, "Redeem Code Limit Reached", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                payableAmountValue = originalAmount - discount;
                                if (payableAmountValue < 0) payableAmountValue = 0.0;
                                payableAmount.setText(String.format("Rs. %.2f", payableAmountValue));
                                redeemCode = code;
                                isRedeemApplied = true;

                                btnApply.setText("Applied");
                                appliedSuccessBtn.setVisibility(View.VISIBLE);
                                appliedSuccessText.setVisibility(View.VISIBLE);

                                if (!isUnlimitedUsage) {
                                    int newUsage = usageLeft - 1;
                                    codeSnap.getRef().child("usage_time").setValue(String.valueOf(newUsage));
                                }
                            } catch (Exception e) {
                                Toast.makeText(PaymentActivity.this, "Error processing redeem code", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(PaymentActivity.this, "Invalid Redeem Code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(PaymentActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePayment() {
        if (payableAmountValue == 0) {
            String paymentId = storePaymentDataAndRegister();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_AGENT_ID, agentId);
            resultIntent.putExtra(EXTRA_SERVANT_MODEL, servantModel);
            resultIntent.putExtra(EXTRA_PAYMENT_AMOUNT, payableAmountValue);
            resultIntent.putExtra(EXTRA_REDEEM_CODE, redeemCode);
            resultIntent.putExtra(EXTRA_AGENT_EMAIL, agentEmailStr);
            resultIntent.putExtra(EXTRA_PAYMENT_ID, paymentId);
            setResult(RESULT_OK, resultIntent);
            ToastUtil.success(this, "Payment Success!");
            finish();
            return;
        }

        Checkout.preload(this);
        payAmountBtn.setEnabled(false);

        new Handler().postDelayed(() -> {
            try {
                Checkout checkout = new Checkout();
                checkout.setKeyID("test_api");
                checkout.setImage(R.drawable.payment_app_logo);
                checkout.setFullScreenDisable(true);

                JSONObject options = new JSONObject();
                options.put("name", "Hire Easy");
                options.put("description", "Servant Registration Payment");
                options.put("currency", "INR");
                options.put("amount", (int) (payableAmountValue * 100));

                JSONObject prefill = new JSONObject();
                prefill.put("email", agentEmailStr);
                prefill.put("contact", agentMobileStr);
                options.put("prefill", prefill);

                checkout.open(PaymentActivity.this, options);
            } catch (Exception e) {
                Toast.makeText(PaymentActivity.this, "Error opening Razorpay: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                payAmountBtn.setEnabled(true);
            }
        }, 500);
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        new Thread(() -> {
            String storedPaymentId = storePaymentDataAndRegister();
            runOnUiThread(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_AGENT_ID, agentId);
                resultIntent.putExtra(EXTRA_SERVANT_MODEL, servantModel);
                resultIntent.putExtra(EXTRA_PAYMENT_AMOUNT, payableAmountValue);
                resultIntent.putExtra(EXTRA_REDEEM_CODE, redeemCode);
                resultIntent.putExtra(EXTRA_AGENT_EMAIL, agentEmailStr);
                resultIntent.putExtra(EXTRA_PAYMENT_ID, storedPaymentId);
                setResult(RESULT_OK, resultIntent);
                ToastUtil.success(this, "Payment Success!");
                finish();
            });
        }).start();
    }

    @Override
    public void onPaymentError(int code, String response) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private String storePaymentDataAndRegister() {
        String paymentId = "SR" + generateRandomString(9);
        DatabaseReference paymentRef = FirebaseDatabase.getInstance().getReference().child("payments").child(paymentId);
        paymentRef.child("payment_id").setValue(paymentId);
        paymentRef.child("agent_id").setValue(agentId);
        paymentRef.child("servant_id").setValue(servantId);
        paymentRef.child("redeem_code").setValue(redeemCode.isEmpty() ? "N/A" : redeemCode);
        paymentRef.child("paid_amount").setValue(String.format("%.2f", payableAmountValue));

        if (servantModel.getId().isEmpty()) {
            servantModel.setId(servantId);
            DatabaseReference servantRef = FirebaseDatabase.getInstance().getReference().child("servants").child(servantId);
            servantRef.setValue(servantModel);
        }
        return paymentId;
    }

    private String generateServantId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("S");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}