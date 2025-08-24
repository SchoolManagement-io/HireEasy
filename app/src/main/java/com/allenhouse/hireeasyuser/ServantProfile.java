package com.allenhouse.hireeasyuser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServantProfile extends BaseActivity {

    private String userId, username, servantId, servantName;
    private DatabaseReference servantsRef, ratingsRef;
    private TextView tvTitle, tvServantId, tvNameValue, tvCategoryValue, tvAreaValue, tvGenderValue, tvExperienceValue, tvSalaryValue, tvUrgentValue, tvMobileValue;
    private ImageView imgProfile, imgVerified;
    private Chip chipAvailable;
    private MaterialButton btnGiveRating;
    private RecyclerView recyclerRatings;
    private ServantRatingAdapter ratingAdapter;
    private List<ServantRatingModel> ratingList;
    private CardView cardProfile;
    private float x1, x2;
    private static final float MIN_SWIPE_DISTANCE = 150;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servant_profile);

        // Initialize Firebase
        servantsRef = FirebaseDatabase.getInstance().getReference("servants");
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");

        // Initialize views
        initializeViews();

        // Get intent data
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        username = intent.getStringExtra("username");
        servantId = intent.getStringExtra("servant_id");
        servantName = intent.getStringExtra("servant_name");

        // Set title with servant's first name
        if (servantName != null && !servantName.isEmpty()) {
            tvTitle.setText(servantName.split(" ")[0] + "'s Profile");
        }

        // Load servant details
        loadServantDetails();

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("user_id", userId);
            returnIntent.putExtra("username", username);
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        // Setup rating button
        btnGiveRating.setOnClickListener(v -> showRatingDialog());

        // Setup swipe gestures
        cardProfile.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                        if (deltaX > 0) {
                            // Right swipe - Open dialer
                            String mobile = tvMobileValue.getText().toString();
                            if (!mobile.isEmpty()) {
                                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                dialIntent.setData(Uri.parse("tel:+91" + mobile));
                                startActivity(dialIntent);
                            }
                        } else {
                            // Left swipe - Open WhatsApp or SMS
                            String mobile = tvMobileValue.getText().toString();
                            if (!mobile.isEmpty()) {
                                try {
                                    Uri uri = Uri.parse("whatsapp://send?phone=+91" + mobile);
                                    Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(whatsappIntent);
                                } catch (Exception e) {
                                    // Fallback to SMS
                                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                                    smsIntent.setData(Uri.parse("sms:+91" + mobile));
                                    startActivity(smsIntent);
                                }
                            }
                        }
                    }
                    break;
            }
            return true;
        });

        // Setup RecyclerView for ratings
        ratingList = new ArrayList<>();
        ratingAdapter = new ServantRatingAdapter(ratingList, userId, this::deleteRating);
        recyclerRatings.setLayoutManager(new LinearLayoutManager(this));
        recyclerRatings.setAdapter(ratingAdapter);

        // Load ratings
        loadRatings();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvServantId = findViewById(R.id.tv_servant_id);
        tvNameValue = findViewById(R.id.tv_name_value);
        tvCategoryValue = findViewById(R.id.tv_category_value);
        tvAreaValue = findViewById(R.id.tv_area_value);
        tvGenderValue = findViewById(R.id.tv_gender_value);
        tvExperienceValue = findViewById(R.id.tv_experience_value);
        tvSalaryValue = findViewById(R.id.tv_salary_value);
        tvUrgentValue = findViewById(R.id.tv_urgent_value);
        tvMobileValue = findViewById(R.id.tv_mobile_value);
        imgProfile = findViewById(R.id.img_profile);
        imgVerified = findViewById(R.id.img_verified);
        chipAvailable = findViewById(R.id.chip_available);
        btnGiveRating = findViewById(R.id.btn_give_rating);
        recyclerRatings = findViewById(R.id.recycler_ratings);
        cardProfile = findViewById(R.id.card_profile);
    }

    private void loadServantDetails() {
        servantsRef.child(servantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String aadharNumber = snapshot.child("aadharNumber").getValue(String.class);
                    String area = snapshot.child("area").getValue(String.class);
                    String availability = snapshot.child("availability").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String experience = snapshot.child("experience").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String expectedSalary = snapshot.child("expectedSalary").getValue(String.class);
                    String urgentCharge = snapshot.child("urgentCharge").getValue(String.class);
                    String profilePhoto = snapshot.child("profilePhoto").getValue(String.class);
                    Boolean isVerified = snapshot.child("isVerified").getValue(Boolean.class);

                    tvServantId.setText(servantId);
                    tvNameValue.setText(name != null ? name : "N/A");
                    tvCategoryValue.setText(category != null ? category : "N/A");
                    tvAreaValue.setText(area != null ? area : "N/A");
                    tvGenderValue.setText(gender != null ? gender : "N/A");
                    tvExperienceValue.setText(experience != null ? experience + " Years" : "N/A");
                    tvSalaryValue.setText(expectedSalary != null ? "Rs. " + expectedSalary : "N/A");
                    tvUrgentValue.setText(urgentCharge != null ? "Rs. " + urgentCharge : "N/A");
                    tvMobileValue.setText(mobile != null ? mobile : "N/A");
                    chipAvailable.setText(availability != null && availability.equals("Yes") ? "Available" : "Not Available");
                    chipAvailable.setTextColor(getResources().getColor(availability != null && availability.equals("Yes") ? R.color.colorSuccess : R.color.colorDanger));

                    if (isVerified != null && isVerified) {
                        imgVerified.setVisibility(View.VISIBLE);
                    } else {
                        imgVerified.setVisibility(View.GONE);
                        findViewById(R.id.tv_verified).setVisibility(View.GONE);
                    }

                    if (profilePhoto != null && !profilePhoto.isEmpty()) {
                        Glide.with(ServantProfile.this)
                                .load(profilePhoto)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    Toast.makeText(ServantProfile.this, "Servant data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServantProfile.this, "Error loading servant data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRatingDialog() {
        // Check if user already rated
        ratingsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasRated = false;
                for (DataSnapshot ratingSnap : snapshot.getChildren()) {
                    String ratedServantId = ratingSnap.child("servantId").getValue(String.class);
                    if (servantId.equals(ratedServantId)) {
                        hasRated = true;
                        break;
                    }
                }

                if (hasRated) {
                    Toast.makeText(ServantProfile.this, "You have already rated this servant.", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServantProfile.this);
                    View dialogView = LayoutInflater.from(ServantProfile.this).inflate(R.layout.give_rating_modal, null);
                    builder.setView(dialogView);

                    TextView etUserName = dialogView.findViewById(R.id.et_user_name);
                    RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
                    EditText etReviewMessage = dialogView.findViewById(R.id.et_review_message);
                    Button btnSubmitRating = dialogView.findViewById(R.id.btn_submit_rating);

                    etUserName.setText(username);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    btnSubmitRating.setOnClickListener(v -> {
                        float rating = ratingBar.getRating();
                        String reviewMessage = etReviewMessage.getText().toString().trim();

                        if (rating == 0) {
                            Toast.makeText(ServantProfile.this, "Please select a rating", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String ratingId = servantName.substring(0, Math.min(4, servantName.length())) + getRandomAlphaNumeric(7);
                        Map<String, Object> ratingData = new HashMap<>();
                        ratingData.put("ratingId", ratingId);
                        ratingData.put("userId", userId);
                        ratingData.put("username", username);
                        ratingData.put("servantId", servantId);
                        ratingData.put("rating", rating);
                        ratingData.put("review", reviewMessage);
                        ratingData.put("timestamp", System.currentTimeMillis());

                        ratingsRef.child(ratingId).setValue(ratingData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ServantProfile.this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    loadRatings();
                                })
                                .addOnFailureListener(e -> Toast.makeText(ServantProfile.this, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServantProfile.this, "Error checking existing ratings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRatings() {
        ratingsRef.orderByChild("servantId").equalTo(servantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingList.clear();
                for (DataSnapshot ratingSnap : snapshot.getChildren()) {
                    String ratingId = ratingSnap.child("ratingId").getValue(String.class);
                    String userId = ratingSnap.child("userId").getValue(String.class);
                    String username = ratingSnap.child("username").getValue(String.class);
                    String servantId = ratingSnap.child("servantId").getValue(String.class);
                    Float rating = ratingSnap.child("rating").getValue(Float.class);
                    String review = ratingSnap.child("review").getValue(String.class);
                    Long timestamp = ratingSnap.child("timestamp").getValue(Long.class);

                    if (ratingId != null && userId != null && username != null && servantId != null && rating != null && timestamp != null) {
                        ServantRatingModel model = new ServantRatingModel(ratingId, userId, username, servantId, rating, review, timestamp);
                        ratingList.add(model);
                    }
                }

                // Sort ratings to show current user's rating first
                ratingList.sort((r1, r2) -> {
                    if (r1.getUserId().equals(userId)) return -1;
                    if (r2.getUserId().equals(userId)) return 1;
                    return Long.compare(r2.getTimestamp(), r1.getTimestamp());
                });

                ratingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServantProfile.this, "Error loading ratings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRating(String ratingId) {
        ratingsRef.child(ratingId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ServantProfile.this, "Rating deleted successfully", Toast.LENGTH_SHORT).show();
                    loadRatings();
                })
                .addOnFailureListener(e -> Toast.makeText(ServantProfile.this, "Failed to delete rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getRandomAlphaNumeric(int count) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }
}