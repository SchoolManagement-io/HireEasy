package com.allenhouse.hireeasyuser;

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

import com.allenhouse.hireeasyuser.R;
import com.allenhouse.hireeasyuser.ServantProfile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PaymentPortal extends BaseActivity implements PaymentResultListener {
    private EditText editRedeem;
    private TextView payableAmount, userEmail, userMobile, subText, appliedSuccessText;
    private ImageButton appliedSuccessBtn;
    private Button btnApply, payAmountBtn;
    private String servantId, servantName, userId, userEmailStr, redeemCode = "";
    private double originalAmount = 20.0;
    private double payableAmountValue = 20.0;
    private DatabaseReference databaseReference;
    private boolean isRedeemApplied = false;
    private ProgressDialog loadingDialog;

    public static final String EXTRA_SERVANT_ID = "servant_id";
    public static final String EXTRA_SERVANT_NAME = "servant_name";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_EMAIL = "user_email";
    public static final String EXTRA_PAYMENT_AMOUNT = "payment_amount";
    public static final String EXTRA_REDEEM_CODE = "redeem_code";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_portal);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        servantId = intent.getStringExtra(EXTRA_SERVANT_ID);
        servantName = intent.getStringExtra(EXTRA_SERVANT_NAME);
        userId = intent.getStringExtra(EXTRA_USER_ID);

        if (servantId == null || servantName == null || userId == null) {
            Toast.makeText(this, "Error: Required data not received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("redeem_codes");

        editRedeem = findViewById(R.id.editRedeem);
        subText = findViewById(R.id.subText);
        payableAmount = findViewById(R.id.payableAmount);
        userEmail = findViewById(R.id.userEmail);
        userMobile = findViewById(R.id.userMobile);
        appliedSuccessBtn = findViewById(R.id.appliedSuccessBtn);
        appliedSuccessText = findViewById(R.id.appliedSuccessText);
        btnApply = findViewById(R.id.btnApply);
        payAmountBtn = findViewById(R.id.payAmountBtn);

        loadUserMobile();

        payableAmount.setText(String.format("₹ %.2f", payableAmountValue));
        subText.setText("To view more about " + servantName + ", unlock their profile for just ₹20.");

        btnApply.setOnClickListener(v -> applyRedeemCode());
        payAmountBtn.setOnClickListener(v -> handlePayment());
    }

    private void loadUserMobile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String mobile = snapshot.child("mobile_number").getValue(String.class);
                    String email = snapshot.child(("email")).getValue(String.class);
                    userMobile.setText(mobile != null ? mobile : "Not available");
                    userEmail.setText(email != null ? email : "Not Available");
                    userEmailStr = email;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentPortal.this, "Failed to load user mobile", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(PaymentPortal.this, "Redeem Code Expired", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                int usageLeft = -1;
                                if (!isUnlimitedUsage) {
                                    usageLeft = Integer.parseInt(usageTime);
                                    if (usageLeft <= 0) {
                                        Toast.makeText(PaymentPortal.this, "Redeem Code Limit Reached", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                payableAmountValue = originalAmount - discount;
                                if (payableAmountValue < 0) payableAmountValue = 0.0;
                                payableAmount.setText(String.format("₹ %.2f", payableAmountValue));
                                redeemCode = code;
                                isRedeemApplied = true;

                                btnApply.setText("Applied");
                                editRedeem.setEnabled(false);
                                appliedSuccessBtn.setVisibility(View.VISIBLE);
                                appliedSuccessText.setVisibility(View.VISIBLE);

                                if (!isUnlimitedUsage) {
                                    int newUsage = usageLeft - 1;
                                    codeSnap.getRef().child("usage_time").setValue(String.valueOf(newUsage));
                                }
                            } catch (Exception e) {
                                Toast.makeText(PaymentPortal.this, "Error processing redeem code", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(PaymentPortal.this, "Invalid Redeem Code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(PaymentPortal.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePayment() {
        if (payableAmountValue == 0) {
            String transactionId = storePaymentData();
            redirectToServantProfile(transactionId);
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
                options.put("description", "Profile Unlock Payment");
                options.put("currency", "INR");
                options.put("amount", (int) (payableAmountValue * 100));

                JSONObject prefill = new JSONObject();
                prefill.put("email", userEmailStr);
                prefill.put("contact", userMobile.getText().toString());
                options.put("prefill", prefill);

                checkout.open(PaymentPortal.this, options);
            } catch (Exception e) {
                Toast.makeText(PaymentPortal.this, "Error opening Razorpay: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                payAmountBtn.setEnabled(true);
            }
        }, 500);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        new Thread(() -> {
            String transactionId = storePaymentData();
            runOnUiThread(() -> {
                Toast.makeText(PaymentPortal.this, "Payment Success!", Toast.LENGTH_SHORT).show();
                redirectToServantProfile(transactionId);
            });
        }).start();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(PaymentPortal.this, "Payment Failed" , Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private String storePaymentData() {
        String transactionId = "TRNC000" + generateRandomString(8);
        String unlockedNum = "UP" + generateRandomString(3).toUpperCase();

        // References
        DatabaseReference unlockPaymentsRef = FirebaseDatabase.getInstance()
                .getReference().child("unlock_payments").child(transactionId);
        DatabaseReference unlockedProfilesRef = FirebaseDatabase.getInstance()
                .getReference().child("unlocked_profiles").child(unlockedNum);

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String paymentDate = sdf.format(new Date());

        // Data for unlock_payments
        unlockPaymentsRef.child("transactionID").setValue(transactionId);
        unlockPaymentsRef.child("userId").setValue(userId);
        unlockPaymentsRef.child("userEmail").setValue(userEmailStr);
        unlockPaymentsRef.child("paid_amount").setValue(String.format("%.2f", payableAmountValue));
        unlockPaymentsRef.child("redeem_code").setValue(redeemCode.isEmpty() ? "N/A" : redeemCode);
        unlockPaymentsRef.child("paymentDate").setValue(paymentDate);

        // Data for unlocked_profiles
        unlockedProfilesRef.child("unlockedNum").setValue(unlockedNum);
        unlockedProfilesRef.child("userId").setValue(userId);
        unlockedProfilesRef.child("servantId").setValue(servantId);
        unlockedProfilesRef.child("servantName").setValue(servantName);
        unlockedProfilesRef.child("unlockedTill").setValue("Lifetime");
        unlockedProfilesRef.child("unlockedWay").setValue("through Profile Payment");

        return transactionId;
    }


    private void redirectToServantProfile(String transactionId) {
        Intent intent = new Intent(PaymentPortal.this, ServantProfile.class);
        intent.putExtra(EXTRA_SERVANT_ID, servantId);
        intent.putExtra(EXTRA_SERVANT_NAME, servantName);
        intent.putExtra(EXTRA_USER_ID, userId);
        startActivity(intent);
        finish();
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