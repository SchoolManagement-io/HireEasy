package com.allenhouse.hireeasy;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginMain extends AppCompatActivity {  // <-- FIXED HERE

    private EditText usernameEditText, emailEditText, passwordEditText;
    private ImageView togglePass;
    private Button loginButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login_main); // Ensure this layout exists

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        togglePass = findViewById(R.id.togglePass);
        loginButton = findViewById(R.id.loginButton);
        int[] eye = {R.drawable.eye, R.drawable.eye_slash};

        // Toggle password visibility
        togglePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                    togglePass.setImageResource(eye[0]);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isPasswordVisible = true;
                    togglePass.setImageResource(eye[1]);
                }
                passwordEditText.setSelection(passwordEditText.getText().length()); // keep cursor at end
            }
        });

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminLoginMain.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if(username.equals("Kanak") && email.equals("kanak@gmail.com") && password.equals("123456")){
                    Toast.makeText(AdminLoginMain.this, "Login Successful (Mock)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
