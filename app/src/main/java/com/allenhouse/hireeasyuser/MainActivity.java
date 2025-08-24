package com.allenhouse.hireeasyuser;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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
    private boolean feedbackAnimated = false;
    private boolean contactAnimated = false;

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
        Button gmailButton = findViewById(R.id.gmail);
        Button phoneDialButton = findViewById(R.id.phoneDial);
        LinearLayout navbar = findViewById(R.id.navbar);
        LinearLayout feedbackContainer = findViewById(R.id.feedback_container);
        LinearLayout contactContainer = findViewById(R.id.contact_container);
        ImageView logo = findViewById(R.id.logo);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedbackRef = database.getReference("feedback");

        // Initialize set to track animated rows
        animatedRows = new HashSet<>();

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation buttonScale = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        Animation slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);

        // Apply initial animations
        navbar.startAnimation(slideUp);
        logo.startAnimation(scaleIn);
        carouselImage.startAnimation(fadeInSlow);
        getStartedButton.startAnimation(bounce);

        // Contact Us: Email button click listener
        gmailButton.setOnClickListener(v -> {
            v.startAnimation(bounce);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:hireeasy@gmail.com"));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Contact Us: Phone dialer button click listener
        phoneDialButton.setOnClickListener(v -> {
            v.startAnimation(bounce);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:+919876543210"));
            try {
                startActivity(dialIntent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "No dialer app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Button click listeners
        loginButton.setOnClickListener(v -> {
            v.startAnimation(fadeIn);
            Intent intent = new Intent(MainActivity.this, UserLoginMain.class);
            startActivity(intent);
        });

        getStartedButton.setOnClickListener(v -> {
            v.startAnimation(buttonScale);
            Intent intent = new Intent(MainActivity.this, UserLoginMain.class);
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
                        ToastUtil.success(MainActivity.this, "Feedback submitted successfully!");
                        feedbackEmail.setText("");
                        feedbackMessage.setText("");
                        // Send thank-you email
                        sendFeedbackEmail(email);
                    })
                    .addOnFailureListener(e -> {
                        ToastUtil.error(MainActivity.this, "Failed to submit feedback: " + e.getMessage());
                    });
        });

        // Carousel implementation with slower fade transition
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                carouselImage.setImageResource(images[currentIndex]);
                carouselImage.startAnimation(fadeInSlow);
                currentIndex = (currentIndex + 1) % images.length;
                handler.postDelayed(this, 4000); // Increased to 4 seconds for smoother transition
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

        // Initial check for visible rows and section animations
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

            // Animate feedback and contact sections only once if visible
            if (!feedbackAnimated && isViewVisible(scrollView, feedbackContainer)) {
                feedbackContainer.startAnimation(slideUp);
                feedbackAnimated = true;
            }

            if (!contactAnimated && isViewVisible(scrollView, contactContainer)) {
                contactContainer.startAnimation(slideUp);
                contactAnimated = true;
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

    // Send thank-you email after feedback submission
    private void sendFeedbackEmail(String toEmail) {
        new Thread(() -> {
            try {
                String subject = "We Appreciate Your Feedback - HireEasy";

                StringBuilder sb = new StringBuilder();
                sb.append("<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;'>")
                        .append("<div style='max-width: 700px; margin: auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>")

                        // Header
                        .append("<div style='background-color: #117A8B; padding: 25px 30px; text-align: center;'>")
                        .append("<img src='https://i.postimg.cc/SjS36vcT/payment-app-logo.png' alt='HireEasy Logo' style='height: 70px; margin-bottom: 10px;' />")
                        .append("<h2 style='color: #ffffff; font-size: 24px; margin: 0;'>Thank You for Your Feedback</h2>")
                        .append("</div>")

                        // Body Content
                        .append("<div style='padding: 30px;'>")
                        .append("<p style='font-size: 16px; color: #333333;'>Dear User,</p>")
                        .append("<p style='font-size: 16px; color: #333333; margin-top: 15px;'>We sincerely appreciate you taking the time to share your feedback with <strong>HireEasy</strong>.</p>")
                        .append("<p style='font-size: 16px; color: #333333; margin-top: 10px;'>Your thoughts help us shape the future of our platform and deliver a better experience for everyone.</p>")
                        .append("<p style='font-size: 15px; color: #6c757d; margin-top: 15px;'>Our team carefully reviews all submissions, and if your feedback requires follow-up, we’ll be in touch shortly.</p>")
                        .append("<p style='font-size: 15px; color: #6c757d;'>Have more ideas or need assistance? Feel free to contact us anytime at <a href='mailto:support@hireeasy.com' style='color: #117A8B; text-decoration: none;'>support@hireeasy.com</a>.</p>")
                        .append("</div>")

                        // Footer
                        .append("<div style='background-color: #f1f1f1; padding: 20px 30px; text-align: center;'>")
                        .append("<p style='font-size: 13px; color: #6c757d; margin: 0;'>You're helping us build a better platform. Thank you for being with <strong>HireEasy</strong>.</p>")
                        .append("<p style='font-size: 13px; color: #6c757d; margin-top: 5px;'>© 2025 HireEasy. All rights reserved.</p>")
                        .append("</div>")

                        .append("</div>")
                        .append("</div>");

                String body = sb.toString();
                GMailSender.send(toEmail, subject, body);
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.error(MainActivity.this, "Failed to send thank-you email: " + e.getMessage()));
            }
        }).start();
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