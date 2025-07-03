package com.allenhouse.hireeasy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserLoginMain extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, mobileEditText, passwordEditText;
    private ImageView togglePass;
    private Button loginButton;
    private boolean isPasswordVisible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login_main);

        // Initialize all views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        mobileEditText = findViewById(R.id.mobileno);
        passwordEditText = findViewById(R.id.password);
        togglePass = findViewById(R.id.togglePass);
        loginButton = findViewById(R.id.loginButton);

        // ✅ Restrict mobile number to 10 digits only
        mobileEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mobileEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        // ✅ Restrict username to alphabets only
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

        // ✅ Hide drawableStart and hint when user types
        setupEditTextBehavior(usernameEditText);
        setupEditTextBehavior(emailEditText);
        setupEditTextBehavior(mobileEditText);
        setupEditTextBehavior(passwordEditText);

        // Toggle password visibility
        togglePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
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
                String mobile = mobileEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
                    Toast.makeText(UserLoginMain.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please enter a valid email address");
                } else if (mobile.length() != 10) {
                    mobileEditText.setError("Mobile number must be exactly 10 digits");
                } else if (!username.matches("[a-zA-Z ]+")) {
                    usernameEditText.setError("Only alphabets allowed in username");
                } else {
                    Toast.makeText(UserLoginMain.this, "Agent Login Successful (Mock)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ✅ Method to hide drawableStart and hint when user types
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
}