package com.allenhouse.hireeasy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.allenhouse.hireeasy.ToastUtil;
import com.allenhouse.hireeasy.SessionManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminLoginMain extends BaseActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private ImageView togglePass;
    private Button loginButton;
    private boolean isPasswordVisible = false;
    private DatabaseReference databaseReference;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_role_login);

        // Initialize Firebase with explicit reference to "admins" collection
        databaseReference = FirebaseDatabase.getInstance().getReference().child("admins");

        // Initialize all views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        togglePass = findViewById(R.id.togglePass);
        loginButton = findViewById(R.id.loginButton);

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
        togglePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    ToastUtil.info(AdminLoginMain.this, "Please fill all fields!");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please enter a valid email address");
                } else if (!username.matches("[a-zA-Z ]+")) {
                    usernameEditText.setError("Only alphabets allowed in username");
                } else {
                    // Hash the input password
                    String hashedPassword = hashPassword(password);

                    // Query Firebase "admins" collection
                    databaseReference.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        ToastUtil.error(AdminLoginMain.this, "No admin found with this email!");
                                        return;
                                    }

                                    boolean isValid = false;
                                    String adminId = null;

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String dbUsername = snapshot.child("username").getValue(String.class);
                                        String dbPassword = snapshot.child("password").getValue(String.class);
                                        adminId = snapshot.child("admin_id").getValue(String.class);

                                        if (dbUsername != null && dbPassword != null && adminId != null &&
                                                dbUsername.equals(username) && dbPassword.equals(hashedPassword)) {
                                            isValid = true;
                                            break;
                                        }
                                    }

                                    if (isValid) {
                                        ToastUtil.success(AdminLoginMain.this, "Login Successful! Admin ID: " + adminId);

                                        // Save admin_id in SessionManager
                                        SessionManager sessionManager = new SessionManager(AdminLoginMain.this);
                                        sessionManager.createSession("admin", adminId);
                                        // Pass admin_id to AdminDashboard
                                        Intent intent = new Intent(AdminLoginMain.this, AdminDashboard.class);
                                        intent.putExtra("admin_id", adminId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        ToastUtil.error(AdminLoginMain.this, "Inavlid Credentials!");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    ToastUtil.error(AdminLoginMain.this, "Database error: " + databaseError.getMessage());
                                }
                            });
                }
            }
        });
    }

    // Method to hide drawableStart and hint when user types
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

    // Hash password using SHA-256
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
            return password; // Fallback to plain password if hashing fails
        }
    }
}