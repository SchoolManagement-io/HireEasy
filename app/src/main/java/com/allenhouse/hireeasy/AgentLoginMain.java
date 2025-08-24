package com.allenhouse.hireeasy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AgentLoginMain extends BaseActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private ImageView togglePass;
    private Button loginButton, forgotPasswordButton;
    private boolean isPasswordVisible = false;
    private DatabaseReference databaseReference, otpReference;
    private ProgressDialog progressDialog;
    private Handler handler;
    private AlertDialog forgotPasswordDialog;
    private EditText forgotEmailEditText, enterOtpEditText, newPasswordEditText;
    private Button sendOtpBtn, resendOtpBtn, btnChangePassword;
    private TextView otpTimerText;
    private View forgotDialogView;
    private String currentOtpId, currentEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agent_role_login);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference().child("agents");
        otpReference = FirebaseDatabase.getInstance().getReference().child("otps");

        // Initialize all views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        togglePass = findViewById(R.id.togglePass);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordButton = findViewById(R.id.forgotPassword);

        // Restrict username to alphabets only
        usernameEditText.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        char c = source.charAt(i);
                        if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Hide drawableStart and hint when user types
        setupEditTextBehavior(usernameEditText);
        setupEditTextBehavior(emailEditText);
        setupEditTextBehavior(passwordEditText);

        // Toggle password visibility
        togglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePass.setImageResource(R.drawable.eye);
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePass.setImageResource(R.drawable.eye_slash);
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                ToastUtil.info(AgentLoginMain.this, "Please fill all fields!");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Please enter a valid email address");
            } else if (!username.matches("[a-zA-Z ]+")) {
                usernameEditText.setError("Only alphabets allowed in username");
            } else {
                String hashedPassword = hashPassword(password);

                databaseReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    ToastUtil.error(AgentLoginMain.this, "No agent found with this email!");
                                    return;
                                }

                                boolean isValid = false;
                                String agentId = null;
                                String dbUsername = null;

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    dbUsername = snapshot.child("username").getValue(String.class);
                                    String dbPassword = snapshot.child("password").getValue(String.class);
                                    agentId = snapshot.child("agent_id").getValue(String.class);

                                    if (dbUsername != null && dbPassword != null && agentId != null &&
                                            dbUsername.equals(username) && dbPassword.equals(hashedPassword)) {
                                        isValid = true;
                                        break;
                                    }
                                }

                                if (isValid) {
                                    ToastUtil.success(AgentLoginMain.this, "Agent Login Successful! Agent ID: " + agentId);

                                    SessionManager sessionManager = new SessionManager(AgentLoginMain.this);
                                    sessionManager.createSession("agent", agentId);

                                    Intent intent = new Intent(AgentLoginMain.this, AgentDashboard.class);
                                    intent.putExtra("agent_id", agentId);
                                    intent.putExtra("username", dbUsername);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    ToastUtil.error(AgentLoginMain.this, "Invalid Credentials!");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                ToastUtil.error(AgentLoginMain.this, "Database error: " + databaseError.getMessage());
                            }
                        });
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        forgotDialogView = LayoutInflater.from(this).inflate(R.layout.forgot_password_dialog, null);
        builder.setView(forgotDialogView);
        builder.setCancelable(true);

        forgotPasswordDialog = builder.create();
        forgotPasswordDialog.show();

        forgotEmailEditText = forgotDialogView.findViewById(R.id.email);
        sendOtpBtn = forgotDialogView.findViewById(R.id.sendOtpBtn);
        otpTimerText = forgotDialogView.findViewById(R.id.otpTimer);
        enterOtpEditText = forgotDialogView.findViewById(R.id.enterOTP);
        resendOtpBtn = forgotDialogView.findViewById(R.id.resendOtpBtn);
        newPasswordEditText = forgotDialogView.findViewById(R.id.newPassword);
        btnChangePassword = forgotDialogView.findViewById(R.id.btnChangePassword);
        ImageButton eyeToggle = forgotDialogView.findViewById(R.id.eyeToggle);

        // Toggle password visibility
        eyeToggle.setOnClickListener(v -> {
            if (newPasswordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                newPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeToggle.setImageResource(R.drawable.eye_slash);
            } else {
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeToggle.setImageResource(R.drawable.eye);
            }
            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
        });

        sendOtpBtn.setOnClickListener(v -> sendOtp());
        resendOtpBtn.setOnClickListener(v -> sendOtp());

        // Auto-verify OTP when 6 digits are entered
        enterOtpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    verifyOtp(); // Call separate OTP verification function
                }
            }
        });

        btnChangePassword.setOnClickListener(v -> changePassword());

        forgotEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    sendOtpBtn.setVisibility(View.VISIBLE);
                } else {
                    sendOtpBtn.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sendOtp() {
        String email = forgotEmailEditText.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ToastUtil.error(this, "Please enter a valid email address");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Delete existing OTP for this email if it exists
        otpReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }

                // Generate new OTP and OTP ID
                String otp = String.format("%06d", new Random().nextInt(999999));
                currentOtpId = "O" + generateRandomString(3);
                currentEmail = email;

                // Store OTP in Firebase
                Map<String, Object> otpData = new HashMap<>();
                otpData.put("otp_id", currentOtpId);
                otpData.put("email", email);
                otpData.put("otp", otp);
                otpReference.child(currentOtpId).setValue(otpData);

                // Send OTP email
                new Thread(() -> {
                    try {
                        String subject = "Reset Your Password - HireEasy";

                        StringBuilder sb = new StringBuilder();
                        sb.append("<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;'>")
                                .append("<div style='max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>")
                                // Header with Logo
                                .append("<div style='background-color: #117A8B; padding: 30px; text-align: center;'>")
                                .append("<img src='https://i.postimg.cc/SjS36vcT/payment-app-logo.png' alt='HireEasy Logo' style='height: 70px; margin-bottom: 10px;'/>")
                                .append("<h1 style='color: #ffffff; font-size: 24px; margin: 0;'>Password Reset OTP</h1>")
                                .append("</div>")
                                // Body Content
                                .append("<div style='padding: 30px;'>")
                                .append("<p style='font-size: 16px; color: #333333; margin-bottom: 20px;'>Hi there,</p>")
                                .append("<p style='font-size: 16px; color: #333333; margin-bottom: 20px;'>We received a request to reset the password associated with your <strong>HireEasy</strong> account. Please use the OTP below to verify your identity and complete the password reset process.</p>")
                                // OTP Box
                                .append("<div style='text-align: center; margin: 30px 0;'>")
                                .append("<span style='display: inline-block; font-size: 32px; font-weight: bold; color: #117A8B; background-color: #e9f7fc; padding: 12px 24px; border-radius: 8px; letter-spacing: 4px;'>")
                                .append(otp)
                                .append("</span>")
                                .append("</div>")
                                // Additional Notes
                                .append("<p style='font-size: 15px; color: #6c757d;'>This OTP is valid for <strong>2 minutes</strong>. Please do not share this code with anyone for your security.</p>")
                                .append("<p style='font-size: 15px; color: #6c757d;'>If you did not request a password reset, no further action is required. You can safely ignore this email or contact our support team for help.</p>")
                                .append("</div>")
                                // Footer
                                .append("<div style='background-color: #f1f1f1; padding: 20px; text-align: center;'>")
                                .append("<p style='font-size: 13px; color: #6c757d; margin: 0;'>Need help? Contact our support team anytime at <a href='mailto:hireeasy1@gmail.com' style='color: #117A8B; text-decoration: none;'>support@hireeasy.com</a></p>")
                                .append("<p style='font-size: 13px; color: #6c757d; margin: 5px 0 0;'>© 2025 HireEasy. All rights reserved.</p>")
                                .append("</div>")
                                .append("</div>")
                                .append("</div>");
                        String body = sb.toString();

                        GMailSender.send(email, subject, body);
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            ToastUtil.success(AgentLoginMain.this, "OTP sent to your " + email);
                            startOtpTimer();
                            updateDialogVisibility(true);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            ToastUtil.error(AgentLoginMain.this, "Failed to send OTP: " + e.getMessage());
                        });
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                ToastUtil.error(AgentLoginMain.this, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void startOtpTimer() {
        final long totalTime = 120000; // 2 minutes in milliseconds
        final long interval = 1000; // Update every second

        handler = new Handler(Looper.getMainLooper());
        final long[] timeLeft = {totalTime};

        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                timeLeft[0] -= interval;
                if (timeLeft[0] >= 0) {
                    long seconds = timeLeft[0] / 1000;
                    long minutes = seconds / 60;
                    seconds = seconds % 60;
                    otpTimerText.setText(String.format("%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, interval);
                } else {
                    otpTimerText.setText("00:00");
                    if (currentOtpId != null) {
                        otpReference.child(currentOtpId).removeValue();
                    }
                    updateDialogVisibility(false);
                }
            }
        };

        handler.post(timerRunnable);
        resendOtpBtn.postDelayed(() -> resendOtpBtn.setVisibility(View.VISIBLE), 20000);

        // Cleanup on dialog dismiss or activity destroy
        forgotPasswordDialog.setOnDismissListener(dialog -> handler.removeCallbacks(timerRunnable));
    }

    private void updateDialogVisibility(boolean otpSent) {
        if (forgotDialogView == null) return;

        View otpSection = forgotDialogView.findViewById(R.id.otpSection);
        View viewOne = forgotDialogView.findViewById(R.id.viewOne);
        View passwordSection = forgotDialogView.findViewById(R.id.passwordSection);
        View viewTwo = forgotDialogView.findViewById(R.id.viewTwo);

        if (otpSent) {
            forgotEmailEditText.setEnabled(false);
            sendOtpBtn.setVisibility(View.GONE);
            otpTimerText.setVisibility(View.VISIBLE);
            otpSection.setVisibility(View.VISIBLE);
            viewOne.setVisibility(View.VISIBLE);
            passwordSection.setVisibility(View.GONE);
            viewTwo.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
            resendOtpBtn.setVisibility(View.GONE);
        } else {
            forgotEmailEditText.setEnabled(true);
            otpTimerText.setVisibility(View.GONE);
            otpSection.setVisibility(View.GONE);
            viewOne.setVisibility(View.GONE);
            passwordSection.setVisibility(View.GONE);
            viewTwo.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
            resendOtpBtn.setVisibility(View.GONE);
        }
    }

    private void verifyOtp() {
        String otp = enterOtpEditText.getText().toString().trim();

        if (otp.length() != 6) {
            ToastUtil.info(this, "Please enter a 6-digit OTP!");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        otpReference.child(currentOtpId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedOtp = dataSnapshot.child("otp").getValue(String.class);
                    if (storedOtp != null && storedOtp.equals(otp)) {
                        progressDialog.dismiss();
                        updateDialogVisibilityForPasswordSection(); // Enable password section
                        ToastUtil.success(AgentLoginMain.this, "OTP verified successfully!");
                    } else {
                        progressDialog.dismiss();
                        ToastUtil.error(AgentLoginMain.this, "Invalid OTP!");
                    }
                } else {
                    progressDialog.dismiss();
                    ToastUtil.error(AgentLoginMain.this, "OTP expired or not found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                ToastUtil.error(AgentLoginMain.this, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateDialogVisibilityForPasswordSection() {
        if (forgotDialogView == null) return;

        View otpSection = forgotDialogView.findViewById(R.id.otpSection);
        View viewOne = forgotDialogView.findViewById(R.id.viewOne);
        View passwordSection = forgotDialogView.findViewById(R.id.passwordSection);
        View viewTwo = forgotDialogView.findViewById(R.id.viewTwo);

        otpSection.setVisibility(View.GONE);
        viewOne.setVisibility(View.GONE);
        passwordSection.setVisibility(View.VISIBLE);
        viewTwo.setVisibility(View.VISIBLE);
        btnChangePassword.setVisibility(View.VISIBLE);
        enterOtpEditText.setEnabled(false); // Disable OTP field after verification
    }

    private void changePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Please enter a new password!");
            return;
        }
        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters long");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Changing password...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String hashedPassword = hashPassword(newPassword);
        databaseReference.orderByChild("email").equalTo(currentEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().child("password").setValue(hashedPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        otpReference.child(currentOtpId).removeValue();
                                        updateDialogVisibility(false); // Reset dialog
                                        ToastUtil.success(AgentLoginMain.this, "Password changed successfully!");
                                        forgotPasswordDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        ToastUtil.error(AgentLoginMain.this, "Failed to change password: " + e.getMessage());
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        ToastUtil.error(AgentLoginMain.this, "Database error: " + databaseError.getMessage());
                    }
                });
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

    private void setupEditTextBehavior(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    editText.setHint("");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (forgotPasswordDialog != null && forgotPasswordDialog.isShowing()) {
            forgotPasswordDialog.dismiss();
        }
    }
}