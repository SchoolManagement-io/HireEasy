package com.allenhouse.hireeasy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
}