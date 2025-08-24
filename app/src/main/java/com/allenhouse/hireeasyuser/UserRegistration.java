package com.allenhouse.hireeasyuser;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRegistration extends BaseActivity {

    private EditText editName, editMobile, editGmail, editOtp, editAadhaar, editPassword;
    private ImageView profileImage, mobileVerifiedIcon;
    private LinearLayout aadhaarVerifiedLayout, otpSection;
    private ImageButton editImageBtn;
    private MaterialButton submitBtn;
    private Button sendOtpBtn, resendOtpBtn;
    private TextView otpTimer;
    private DatabaseReference databaseReference, otpReference;
    private boolean isOtpVerified = false;
    private String currentImageUrl;
    private CountDownTimer countDownTimer;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final String IMGBB_API_KEY = "2b7a203099dc367c13140ecb2a20ea9e";
    private static final long OTP_TIMEOUT = 120_000; // 2 minutes in milliseconds
    private static final long RESEND_DELAY = 20_000; // 20 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_role_register);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        otpReference = FirebaseDatabase.getInstance().getReference("otps");

        // Initialize views
        editName = findViewById(R.id.tvName);
        editMobile = findViewById(R.id.mobile);
        editGmail = findViewById(R.id.gmail);
        editOtp = findViewById(R.id.otp);
        editAadhaar = findViewById(R.id.aadhar_number);
        editPassword = findViewById(R.id.password);
        profileImage = findViewById(R.id.profile_picture);
        mobileVerifiedIcon = findViewById(R.id.mobile_verified_icon);
        aadhaarVerifiedLayout = findViewById(R.id.aadhaar_verified);
        editImageBtn = findViewById(R.id.btn_edit_image);
        submitBtn = findViewById(R.id.submit_btn);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);
        resendOtpBtn = findViewById(R.id.resendOtpBtn);
        otpSection = findViewById(R.id.otpSection);
        otpTimer = findViewById(R.id.otpTimer);

        // Initially disable password field
        editPassword.setEnabled(false);

        // Setup input validation
        setupInputValidation();

        // Image upload button listener
        editImageBtn.setOnClickListener(v -> checkAndRequestPermissions());

        // Send OTP button listener
        sendOtpBtn.setOnClickListener(v -> {
            sendOtpBtn.setEnabled(false);
            sendOtp();
        });

        // Resend OTP button listener
        resendOtpBtn.setOnClickListener(v -> {
            String gmail = editGmail.getText().toString().trim();
            otpReference.child(encodeEmail(gmail)).removeValue();
            sendOtp();
        });

        // OTP auto-verification
        editOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String otp = s.toString().trim();
                String gmail = editGmail.getText().toString().trim();
                if (otp.length() == 6 && !gmail.isEmpty()) {
                    verifyOtp(otp, gmail);
                } else {
                    isOtpVerified = false;
                    editPassword.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Submit button listener
        submitBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                saveToFirebase();
            }
        });

        // Email validation for OTP button visibility
        editGmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String gmail = s.toString().trim();
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
                    sendOtpBtn.setVisibility(View.VISIBLE);
                } else {
                    sendOtpBtn.setVisibility(View.GONE);
                    otpSection.setVisibility(View.GONE);
                    isOtpVerified = false;
                    editPassword.setEnabled(false);
                    stopTimer();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupInputValidation() {
        // Name: Only alphabets and spaces
        editName.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isWhitespace(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Mobile: 10 digits, starting with 6-9
        editMobile.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(10),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        editMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mobile = s.toString().trim();
                mobileVerifiedIcon.setVisibility(mobile.matches("^[6-9][0-9]{9}$") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Aadhaar: 12 digits, not starting with 0 or 1
        editAadhaar.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(12),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        editAadhaar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String aadhaar = s.toString().trim();
                aadhaarVerifiedLayout.setVisibility(aadhaar.matches("^[2-9][0-9]{11}$") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sendOtp() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String gmail = editGmail.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            editGmail.setError("Valid email is required");
            ToastUtil.error(UserRegistration.this, "Invalid email");
            progressDialog.dismiss();
            return;
        }

        String otp = generateOtp();
        String otpId = generateOtpId();
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp_id", otpId);
        otpData.put("email", gmail);
        otpData.put("otp", otp);

        otpReference.child(encodeEmail(gmail)).setValue(otpData, (error, ref) -> {
            if (error == null) {
                String subject = "Your OTP Code - HireEasy";
                StringBuilder sb = new StringBuilder();
                sb.append("<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #6C757D; border-radius: 10px; background-color: #F8F9FA; max-width: 500px; margin: auto;'>")
                        .append("<div style='text-align: center;'>")
                        .append("<img src=\"https://i.postimg.cc/SjS36vcT/payment-app-logo.png\" alt=\"HireEasy Logo\" style=\"height: 80px; margin-bottom: 20px; display: block;\" />\n")
                        .append("<h2 style='color: #212529;'>Your OTP Code</h2>")
                        .append("</div>")
                        .append("<p style='font-size: 16px; color: #212529;'>Dear User,</p>")
                        .append("<p style='font-size: 16px; color: #212529;'>Your One-Time Password (OTP) for registration is:</p>")
                        .append("<div style='text-align: center; margin: 20px 0;'>")
                        .append("<span style='font-size: 28px; font-weight: bold; color: #007BFF; letter-spacing: 4px;'>")
                        .append(otp)
                        .append("</span>")
                        .append("</div>")
                        .append("<p style='font-size: 15px; color: #6C757D;'>This OTP is valid for <b>2 minutes</b>. Please do not share it with anyone.</p>")
                        .append("<hr style='margin: 30px 0;'>")
                        .append("<p style='text-align: center; font-size: 14px; color: #6C757D;'>Thank you for choosing <strong>HireEasy</strong>.<br>— Team HireEasy</p>")
                        .append("</div>");

                String body = sb.toString();

                new Thread(() -> {
                    try {
                        GMailSender.send(gmail, subject, body);
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            ToastUtil.success(this, "OTP sent to your " + gmail);
                            otpSection.setVisibility(View.VISIBLE);
                            editGmail.setEnabled(false);
                            sendOtpBtn.setVisibility(View.GONE);
                            startOtpTimer();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            ToastUtil.error(this, "Failed to send OTP: " + e.getMessage());
                        });
                    }
                }).start();
            } else {
                progressDialog.dismiss();
                ToastUtil.error(this, "Failed to save OTP: " + error.getMessage());
            }
        });
    }

    private void verifyOtp(String otp, String gmail) {
        otpReference.child(encodeEmail(gmail)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedOtp = snapshot.child("otp").getValue(String.class);
                    if (storedOtp != null && storedOtp.equals(otp)) {
                        isOtpVerified = true;
                        editPassword.setEnabled(true);
                        otpSection.setVisibility(View.GONE);
                        editPassword.setVisibility(View.VISIBLE);
                        editGmail.setEnabled(false);
                        ToastUtil.success(UserRegistration.this, "OTP verified!");
                        otpReference.child(encodeEmail(gmail)).removeValue();
                        stopTimer();
                    } else {
                        isOtpVerified = false;
                        editPassword.setEnabled(false);
                        editOtp.setError("Invalid OTP");
                    }
                } else {
                    isOtpVerified = false;
                    editPassword.setEnabled(false);
                    editOtp.setError("OTP expired or invalid");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtil.error(UserRegistration.this, "OTP verification failed: " + error.getMessage());
            }
        });
    }

    private void startOtpTimer() {
        stopTimer(); // Stop any existing timer
        resendOtpBtn.setVisibility(View.GONE);
        otpTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(OTP_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                otpTimer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                if (millisUntilFinished <= (OTP_TIMEOUT - RESEND_DELAY)) {
                    resendOtpBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFinish() {
                otpTimer.setText("00:00");
                String gmail = editGmail.getText().toString().trim();
                otpReference.child(encodeEmail(gmail)).removeValue();
                ToastUtil.error(UserRegistration.this, "OTP expired");
                otpSection.setVisibility(View.GONE);
                sendOtpBtn.setVisibility(View.VISIBLE);
                resendOtpBtn.setVisibility(View.GONE);
                otpTimer.setVisibility(View.GONE);
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            resendOtpBtn.setVisibility(View.GONE);
            otpTimer.setVisibility(View.GONE);
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private String generateOtpId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("O");
        for (int i = 0; i < 3; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateUserId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("U");
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
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

    private void saveToFirebase() {
        if (!isOtpVerified) {
            ToastUtil.error(this, "Please verify OTP first!");
            return;
        }

        String name = editName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();
        String gmail = editGmail.getText().toString().trim();
        String aadhaar = editAadhaar.getText().toString().trim();
        String password = hashPassword(editPassword.getText().toString().trim());
        String userId = generateUserId();

        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", userId);
        userData.put("username", name);
        userData.put("mobile_number", mobile);
        userData.put("email", gmail);
        userData.put("aadhaar", aadhaar);
        userData.put("password", password);
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            userData.put("profile_photo", currentImageUrl);
        }

        databaseReference.child(userId).setValue(userData, (error, ref) -> {
            if (error == null) {
                ToastUtil.success(UserRegistration.this, "Registration successful!");
                Intent intent = new Intent(UserRegistration.this, UserLoginMain.class);
                startActivity(intent);
                finish();
            } else {
                ToastUtil.error(UserRegistration.this, "Failed to register: " + error.getMessage());
            }
        });
    }

    private boolean validateInputs() {
        String name = editName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();
        String gmail = editGmail.getText().toString().trim();
        String aadhaar = editAadhaar.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Name is required");
            return false;
        }
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            editName.setError("Only alphabets allowed in name");
            return false;
        }
        if (!mobile.matches("^[6-9][0-9]{9}$")) {
            editMobile.setError("Valid 10-digit mobile number is required");
            return false;
        }
        if (!aadhaar.matches("^[2-9][0-9]{11}$")) {
            editAadhaar.setError("Valid 12-digit Aadhaar number is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            editGmail.setError("Valid email is required");
            return false;
        }
        if (!isOtpVerified) {
            editOtp.setError("Please verify OTP");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            showImageOptions();
        }
    }

    private void showImageOptions() {
        String[] options = {"Upload from Camera", "Upload from Gallery", "Delete Image"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else if (which == 1) openGallery();
                    else if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        currentImageUrl = null;
                        profileImage.setImageResource(R.drawable.default_profile);
                    } else {
                        ToastUtil.info(this, "No custom image to delete!");
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            ToastUtil.error(this, "Camera app not found");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_GALLERY);
        } else {
            ToastUtil.error(this, "Gallery app not found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap;
                if (requestCode == REQUEST_CAMERA) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    profileImage.setImageBitmap(bitmap);
                    uploadImageToImgBB(bitmap);
                } else if (requestCode == REQUEST_GALLERY) {
                    Uri selectedImage = data.getData();
                    profileImage.setImageURI(selectedImage);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    uploadImageToImgBB(bitmap);
                }
            } catch (IOException e) {
                ToastUtil.error(this, "Error loading image: " + e.getMessage());
            }
        }
    }

    private void uploadImageToImgBB(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "user_image.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                .addFormDataPart("key", IMGBB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> ToastUtil.error(UserRegistration.this, "Image upload failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            currentImageUrl = json.getJSONObject("data").getString("url");
                            runOnUiThread(() -> ToastUtil.success(UserRegistration.this, "Image uploaded successfully!"));
                        } else {
                            runOnUiThread(() -> ToastUtil.error(UserRegistration.this, "Image upload failed"));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> ToastUtil.error(UserRegistration.this, "Error parsing response: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> ToastUtil.error(UserRegistration.this, "Image upload failed: " + response.message()));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                showImageOptions();
            } else {
                ToastUtil.error(this, "Required permissions denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}