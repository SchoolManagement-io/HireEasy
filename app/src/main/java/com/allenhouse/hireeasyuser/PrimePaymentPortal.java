package com.allenhouse.hireeasyuser;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PrimePaymentPortal extends BaseActivity implements PaymentResultListener {
    private EditText editRedeem;
    private TextView payableAmount, userEmail, userMobile, subText, appliedSuccessText, titleText;
    private ImageButton appliedSuccessBtn;
    private Button btnApply;
    private com.google.android.material.button.MaterialButton payAmountBtn;
    private String userId, userName, userEmailStr, redeemCode = "";
    private double originalAmount = 499.0;
    private double payableAmountValue = 499.0;
    private DatabaseReference databaseReference, primeMembersRef, usersRef;
    private boolean isRedeemApplied = false;
    private ProgressDialog loadingDialog;

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_EMAIL = "user_email";
    public static final String EXTRA_PAYMENT_AMOUNT = "payment_amount";
    public static final String EXTRA_REDEEM_CODE = "redeem_code";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prime_membership_payment);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_USER_ID);
        userName = intent.getStringExtra(EXTRA_USER_NAME);
        userEmailStr = intent.getStringExtra(EXTRA_USER_EMAIL);

        if (userId == null || userName == null || userEmailStr == null) {
            Toast.makeText(this, "Error: Required data not received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("redeem_codes");
        primeMembersRef = FirebaseDatabase.getInstance().getReference().child("prime_members");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        editRedeem = findViewById(R.id.editRedeem);
        subText = findViewById(R.id.subText);
        payableAmount = findViewById(R.id.payableAmount);
        userEmail = findViewById(R.id.userEmail);
        userMobile = findViewById(R.id.userMobile);
        appliedSuccessBtn = findViewById(R.id.appliedSuccessBtn);
        appliedSuccessText = findViewById(R.id.appliedSuccessText);
        btnApply = findViewById(R.id.btnApply);
        payAmountBtn = findViewById(R.id.payAmountBtn);
        titleText = findViewById(R.id.titleText);

        loadUserMobile();

        payableAmount.setText(String.format("₹ %.2f", payableAmountValue));
        subText.setText("Get unlimited access to detailed profile of any servant. One-time payment of just ₹499/month.");
        titleText.setText("Unlock Prime Membership for " + userName);
        userEmail.setText(userEmailStr);

        btnApply.setOnClickListener(v -> applyRedeemCode());
        payAmountBtn.setOnClickListener(v -> handlePayment());
    }

    private void loadUserMobile() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String mobile = snapshot.child("mobile_number").getValue(String.class);
                    userMobile.setText(mobile != null ? mobile : "Not available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PrimePaymentPortal.this, "Failed to load user mobile", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(PrimePaymentPortal.this, "Redeem Code Expired", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                int usageLeft = -1;
                                if (!isUnlimitedUsage) {
                                    usageLeft = Integer.parseInt(usageTime);
                                    if (usageLeft <= 0) {
                                        Toast.makeText(PrimePaymentPortal.this, "Redeem Code Limit Reached", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(PrimePaymentPortal.this, "Error processing redeem code", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(PrimePaymentPortal.this, "Invalid Redeem Code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(PrimePaymentPortal.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePayment() {
        if (payableAmountValue == 0) {
            storePaymentData();
            redirectToDashboard();
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
                options.put("description", "Prime Membership Payment");
                options.put("currency", "INR");
                options.put("amount", (int) (payableAmountValue * 100));

                JSONObject prefill = new JSONObject();
                prefill.put("email", userEmailStr);
                prefill.put("contact", userMobile.getText().toString());
                options.put("prefill", prefill);

                checkout.open(PrimePaymentPortal.this, options);
            } catch (Exception e) {
                Toast.makeText(PrimePaymentPortal.this, "Error opening Razorpay: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                payAmountBtn.setEnabled(true);
            }
        }, 500);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        new Thread(() -> {
            storePaymentData();
            runOnUiThread(() -> {
                Toast.makeText(PrimePaymentPortal.this, "Payment Success!", Toast.LENGTH_SHORT).show();
                redirectToDashboard();
            });
        }).start();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(PrimePaymentPortal.this, "Payment Failed", Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void storePaymentData() {
        String transactionId = "TRNC000" + generateRandomString(8);
        String membershipId = "MEM" + generateRandomString(8); // Unique ID for each membership entry

        // References
        DatabaseReference unlockPaymentsRef = FirebaseDatabase.getInstance()
                .getReference().child("unlock_payments").child(transactionId);
        DatabaseReference primeMembersRef = FirebaseDatabase.getInstance()
                .getReference().child("prime_members").child(membershipId);

        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String[] startDate = new String[1];
        final String[] endingDate = new String[1];
        Date currentDate = new Date();

        // Check for previous membership to get the latest endingDate
        primeMembersRef.getParent().orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String primeNumber = null;
                        String previousEndDate = null;

                        // Find the latest membership entry for the user
                        DataSnapshot latestSnapshot = null;
                        for (DataSnapshot memberSnap : snapshot.getChildren()) {
                            String endDateStr = memberSnap.child("endingDate").getValue(String.class);
                            if (endDateStr != null) {
                                try {
                                    Date endDate = sdf.parse(endDateStr);
                                    if (latestSnapshot == null || (endDate != null && endDate.after(sdf.parse(latestSnapshot.child("endingDate").getValue(String.class))))) {
                                        latestSnapshot = memberSnap;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (latestSnapshot != null) {
                            primeNumber = latestSnapshot.child("primeNumber").getValue(String.class);
                            previousEndDate = latestSnapshot.child("endingDate").getValue(String.class);
                        }

                        // Generate new primeNumber if none exists
                        if (primeNumber == null) {
                            primeNumber = generateRandomString(16);
                        }

                        // Set startDate to today or previous endDate if it exists and is later than today
                        startDate[0] = sdf.format(currentDate);
                        try {
                            if (previousEndDate != null) {
                                Date prevEndDate = sdf.parse(previousEndDate);
                                if (prevEndDate != null && prevEndDate.after(currentDate)) {
                                    startDate[0] = previousEndDate;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Calculate endingDate (30 days from startDate)
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(startDate[0]));
                        } catch (Exception e) {
                            calendar.setTime(currentDate);
                        }
                        calendar.add(Calendar.DAY_OF_MONTH, 30);
                        endingDate[0] = sdf.format(calendar.getTime());

                        // Data for unlock_payments
                        unlockPaymentsRef.child("transactionID").setValue(transactionId);
                        unlockPaymentsRef.child("userId").setValue(userId);
                        unlockPaymentsRef.child("userEmail").setValue(userEmailStr);
                        unlockPaymentsRef.child("paid_amount").setValue(String.format("%.2f", payableAmountValue));
                        unlockPaymentsRef.child("redeem_code").setValue(redeemCode.isEmpty() ? "N/A" : redeemCode);
                        unlockPaymentsRef.child("paymentDate").setValue(sdf.format(currentDate));

                        // Data for prime_members
                        primeMembersRef.child("primeNumber").setValue(primeNumber);
                        primeMembersRef.child("userId").setValue(userId);
                        primeMembersRef.child("startDate").setValue(startDate[0]);
                        primeMembersRef.child("endingDate").setValue(endingDate[0]);

                        // Send confirmation email
                        sendMembershipConfirmationEmail(startDate[0], endingDate[0]);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PrimePaymentPortal.this, "Error checking existing membership", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMembershipConfirmationEmail(String startDate, String endingDate) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending confirmation email...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String subject = "HireEasy Prime Membership Confirmation";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #6C757D; border-radius: 10px; background-color: #F8F9FA; max-width: 500px; margin: auto;'>")
                .append("<div style='text-align: center;'>")
                .append("<img src=\"https://i.postimg.cc/SjS36vcT/payment-app-logo.png\" alt=\"HireEasy Logo\" style=\"height: 80px; margin-bottom: 20px; display: block;\" />\n")
                .append("<h2 style='color: #212529;'>Prime Membership Confirmation</h2>")
                .append("</div>")
                .append("<p style='font-size: 16px; color: #212529;'>Dear ").append(userName).append(",</p>")
                .append("<p style='font-size: 16px; color: #212529;'>Thank you for purchasing a HireEasy Prime Membership!</p>")
                .append("<p style='font-size: 16px; color: #212529;'>Here are your membership details:</p>")
                .append("<div style='margin: 20px 0;'>")
                .append("<p style='font-size: 16px; color: #212529;'><strong>Membership Type:</strong> Prime Membership</p>")
                .append("<p style='font-size: 16px; color: #212529;'><strong>Start Date:</strong> ").append(startDate).append("</p>")
                .append("<p style='font-size: 16px; color: #212529;'><strong>End Date:</strong> ").append(endingDate).append("</p>")
                .append("<p style='font-size: 16px; color: #212529;'><strong>Amount Paid:</strong> ₹ ").append(String.format("%.2f", payableAmountValue)).append("</p>")
                .append(redeemCode.isEmpty() ? "" : "<p style='font-size: 16px; color: #212529;'><strong>Redeem Code:</strong> " + redeemCode + "</p>")
                .append("</div>")
                .append("<p style='font-size: 15px; color: #6C757D;'>Your membership is valid until <b>").append(endingDate).append("</b>. Enjoy unlimited access to detailed profiles and exclusive features!</p>")
                .append("<hr style='margin: 30px 0;'>")
                .append("<p style='text-align: center; font-size: 14px; color: #6C757D;'>Thank you for choosing <strong>HireEasy</strong>.<br>— Team HireEasy</p>")
                .append("</div>");

        String body = sb.toString();

        new Thread(() -> {
            try {
                GMailSender.send(userEmailStr, subject, body);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(PrimePaymentPortal.this, "Confirmation email sent to " + userEmailStr, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(PrimePaymentPortal.this, "Failed to send confirmation email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(PrimePaymentPortal.this, UserDashboard.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("username", userName);
        startActivity(intent);
        finish();
    }

    private String generateRandomString(int length) {
        String chars = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}