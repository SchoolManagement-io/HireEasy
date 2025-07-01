package com.allenhouse.hireeasy;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

class AgentLoginMain extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, mobileEditText, passwordEditText;
    private ImageView togglePass;
    private Button loginButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agent_login_main);  // Make sure your XML file is named exactly like this

        // Initialize all views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        mobileEditText = findViewById(R.id.mobileno);
        passwordEditText = findViewById(R.id.password);
        togglePass = findViewById(R.id.togglePass);
        loginButton = findViewById(R.id.loginButton);

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
                    Toast.makeText(AgentLoginMain.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Implement real login logic here (API, Firebase, etc.)
                    Toast.makeText(AgentLoginMain.this, "Agent Login Successful (Mock)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
