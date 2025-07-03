package com.allenhouse.hireeasy;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_role_selection);

        // Initialize views
        TextView welcomeText = findViewById(R.id.welcomeText);
        CardView adminCard = findViewById(R.id.adminCard);
        CardView agentCard = findViewById(R.id.agentCard);
        CardView userCard = findViewById(R.id.userCard);
        LinearLayout roleOptions = findViewById(R.id.roleOptions);

        // Set welcome text and orientation based on screen dimensions
        updateLayoutBasedOnOrientation(welcomeText, roleOptions);

        // Apply animations
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_sequential);
        Animation cardScale = AnimationUtils.loadAnimation(this, R.anim.card_scale);

        // Welcome text bounce animation
        welcomeText.startAnimation(bounce);

        // Sequential fade-in for cards
        adminCard.startAnimation(fadeIn);
        fadeIn.setStartOffset(200);
        agentCard.startAnimation(fadeIn);
        fadeIn.setStartOffset(400);
        userCard.startAnimation(fadeIn);

        // Card click listeners with scale animation
        adminCard.setOnClickListener(v -> {
            v.startAnimation(cardScale);
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        agentCard.setOnClickListener(v -> {
            v.startAnimation(cardScale);
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        userCard.setOnClickListener(v -> {
            v.startAnimation(cardScale);
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView welcomeText = findViewById(R.id.welcomeText);
        LinearLayout roleOptions = findViewById(R.id.roleOptions);
        updateLayoutBasedOnOrientation(welcomeText, roleOptions);
    }

    private void updateLayoutBasedOnOrientation(TextView welcomeText, LinearLayout roleOptions) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (screenWidth < screenHeight) {
            // Portrait mode: single line text, vertical card orientation
            welcomeText.setText("Welcome to\nHireEasy");
            roleOptions.setOrientation(LinearLayout.VERTICAL);
        } else {
            // Landscape mode: text with newline, horizontal card orientation
            welcomeText.setText("Welcome to HireEasy");
            roleOptions.setOrientation(LinearLayout.HORIZONTAL);
        }
    }
}