package com.allenhouse.hireeasy;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_role_selection);

        // Initialize views
        TextView welcomeText = findViewById(R.id.welcomeText);
        CardView adminCard = findViewById(R.id.adminCard);
        CardView agentCard = findViewById(R.id.agentCard);
        LinearLayout roleOptions = findViewById(R.id.roleOptions);

        // Update layout based on orientation
        updateLayoutBasedOnOrientation(welcomeText, roleOptions);

        // Welcome text animation (slide in and fade)
        ObjectAnimator welcomeFade = ObjectAnimator.ofFloat(welcomeText, "alpha", 0f, 1f);
        ObjectAnimator welcomeSlide = ObjectAnimator.ofFloat(welcomeText, "translationY", 100f, 0f);
        AnimatorSet welcomeSet = new AnimatorSet();
        welcomeSet.playTogether(welcomeFade, welcomeSlide);
        welcomeSet.setDuration(800);
        welcomeSet.start();

        // Card animations (fade in, scale up, slight rotation)
        animateCard(adminCard, 200);
        animateCard(agentCard, 400);

        // Card click listeners with scale and elevation effect
        adminCard.setOnClickListener(v -> {
            performCardClickAnimation(v, AdminLoginMain.class);
        });

        agentCard.setOnClickListener(v -> {
            performCardClickAnimation(v, AgentLoginMain.class);
        });
    }

    private void animateCard(View view, long delay) {
        ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "translationZ", 0f, 12f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", -5f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fade, scaleX, scaleY, elevation, rotation);
        animatorSet.setDuration(600);
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    private void performCardClickAnimation(View view, Class<?> destination) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "translationZ", 12f, 20f, 12f);

        AnimatorSet clickSet = new AnimatorSet();
        clickSet.playTogether(scaleDownX, scaleDownY, elevation);
        clickSet.setDuration(200);
        clickSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(LoginActivity.this, destination);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        clickSet.start();
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