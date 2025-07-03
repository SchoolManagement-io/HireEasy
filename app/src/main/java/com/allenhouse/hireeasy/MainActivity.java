package com.allenhouse.hireeasy;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends BaseActivity {

    private ImageView carouselImage;
    private Handler handler;
    private Runnable runnable;
    private int[] images = {R.drawable.carousel1, R.drawable.carousel2, R.drawable.carousel3};
    private int currentIndex = 0;
    private Set<Integer> animatedRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        Button loginButton = findViewById(R.id.login_button);
        Button getStartedButton = findViewById(R.id.get_started_button);
        Button submitFeedbackButton = findViewById(R.id.submit_feedback_button);
        EditText feedbackEmail = findViewById(R.id.feedback_email);
        EditText feedbackMessage = findViewById(R.id.feedback_message);
        carouselImage = findViewById(R.id.carousel_image);
        ScrollView scrollView = findViewById(R.id.scroll_view);
        LinearLayout featuresContainer = findViewById(R.id.features_container);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedbackRef = database.getReference("feedback");

        // Initialize set to track animated rows
        animatedRows = new HashSet<>();

        // Animations
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation buttonScale = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        Animation slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right);

        // Button click listeners
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

        submitFeedbackButton.setOnClickListener(v -> {
            v.startAnimation(buttonScale);
            String email = feedbackEmail.getText().toString().trim();
            String message = feedbackMessage.getText().toString().trim();

            if (email.isEmpty() || message.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate feedback ID (F + 4 random characters)
            String feedbackId = "F" + generateRandomString(4);

            // Create feedback object
            Feedback feedback = new Feedback(feedbackId, email, message);

            // Save to Firebase
            feedbackRef.child(feedbackId).setValue(feedback)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                        feedbackEmail.setText("");
                        feedbackMessage.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    });
        });

        // Carousel implementation
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                carouselImage.setImageResource(images[currentIndex]);
                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                carouselImage.startAnimation(fadeIn);
                currentIndex = (currentIndex + 1) % images.length;
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);

        // Scroll listener for feature card animations
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int[] rowIds = {R.id.feature_row_1, R.id.feature_row_2, R.id.feature_row_3};
            int[] leftCardIds = {R.id.card_1, R.id.card_3, R.id.card_5};
            int[] rightCardIds = {R.id.card_2, R.id.card_4, R.id.card_6};

            for (int i = 0; i < rowIds.length; i++) {
                LinearLayout row = findViewById(rowIds[i]);
                if (!animatedRows.contains(rowIds[i]) && isViewVisible(scrollView, row)) {
                    LinearLayout leftCard = findViewById(leftCardIds[i]);
                    LinearLayout rightCard = findViewById(rightCardIds[i]);
                    leftCard.startAnimation(slideLeft);
                    rightCard.startAnimation(slideRight);
                    animatedRows.add(rowIds[i]);
                }
            }
        });

        // Initial check for visible rows
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int[] rowIds = {R.id.feature_row_1, R.id.feature_row_2, R.id.feature_row_3};
            int[] leftCardIds = {R.id.card_1, R.id.card_3, R.id.card_5};
            int[] rightCardIds = {R.id.card_2, R.id.card_4, R.id.card_6};

            for (int i = 0; i < rowIds.length; i++) {
                LinearLayout row = findViewById(rowIds[i]);
                if (!animatedRows.contains(rowIds[i]) && isViewVisible(scrollView, row)) {
                    LinearLayout leftCard = findViewById(leftCardIds[i]);
                    LinearLayout rightCard = findViewById(rightCardIds[i]);
                    leftCard.startAnimation(slideLeft);
                    rightCard.startAnimation(slideRight);
                    animatedRows.add(rowIds[i]);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    // Check if a view is visible in the ScrollView
    private boolean isViewVisible(ScrollView scrollView, View view) {
        Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        int top = view.getTop();
        int bottom = top + view.getHeight();
        int scrollY = scrollView.getScrollY();
        int height = scrollView.getHeight();
        return top >= scrollY && bottom <= scrollY + height;
    }

    // Generate random string for feedback ID
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    // Feedback class for Firebase
    public static class Feedback {
        public String feedbackId;
        public String email;
        public String message;

        public Feedback() {
            // Default constructor required for Firebase
        }

        public Feedback(String feedbackId, String email, String message) {
            this.feedbackId = feedbackId;
            this.email = email;
            this.message = message;
        }
    }
}