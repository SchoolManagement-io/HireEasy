package com.allenhouse.hireeasy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView carouselImage;
    private TextView carouselText;
    private Handler handler;
    private Runnable runnable;
    private int[] images = {R.drawable.carousel1, R.drawable.carousel2, R.drawable.carousel3};
    int[] ham = {R.drawable.hamburger, R.drawable.hamburger_vertical};
    private String[] texts = {
            "Welcome to HireEasy!",
            "Hire Top Talent Fast!",
            "Streamline Your Recruitment!"
    };
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        Button loginButton = findViewById(R.id.login_button);
        Button getStartedButton = findViewById(R.id.get_started_button);
        Button contactFeedbackButton = findViewById(R.id.contact_feedback_button);
        carouselImage = findViewById(R.id.carousel_image);
        carouselText = findViewById(R.id.carousel_text);

        // Hamburger menu toggle with animation
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation buttonScale = AnimationUtils.loadAnimation(this, R.anim.button_scale);

        // Apply scale animation to all buttons
        View.OnClickListener buttonClickListener = v -> {
            v.startAnimation(buttonScale);
        };

        loginButton.setOnClickListener(v -> {
            v.startAnimation(buttonScale);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        getStartedButton.setOnClickListener(v -> {
            v.startAnimation(buttonScale);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        contactFeedbackButton.setOnClickListener(v -> {
            v.startAnimation(buttonScale);
            Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        // Carousel implementation
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                carouselImage.setImageResource(images[currentIndex]);
                carouselText.setText(texts[currentIndex]);
                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                carouselImage.startAnimation(fadeIn);
                carouselText.startAnimation(fadeIn);
                currentIndex = (currentIndex + 1) % images.length;
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}