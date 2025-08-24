package com.allenhouse.hireeasyuser;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserDashboard extends BaseActivity {

    private TextView greet, heading, subheading, popularServicesText, recommendText, unlockedText;
    private ImageView profilePic, filterSpinnerArrow, membership_button, sortSpinnerArrow;
    private Button unlockBtn;
    private ImageButton homeSectionButton, searchSectionButton, membershipSectionButton, unlockSectionButton, profileSectionButton, imageUnlockBtn;
    private AppCompatSpinner filterSpinner, sortSpinner;
    private RecyclerView popularRecyclerView, recommendRecyclerView, servantRecyclerView, servantUnlockedRecyclerView;
    private LinearLayout searchSection, profileRootButtons, unlockedProfilesSection, profileSection, mainContentLayout, layoutUnlockBtn, filterSortContainer, membershipSection;
    private RelativeLayout headerLayout;
    private ScrollView homeSection, searchScrollView, unlockedContainer, membershipScrollView, profileScrollView;
    private com.google.android.material.button.MaterialButton fabQuickHelp, btnViewProfile;
    private EditText editFilter;
    private DatabaseReference databaseReference, primeMembersRef, usersRef, servantsRef, ratingsRef, unlockedProfilesRef;
    private String userId, username;
    private CategoryAdapter categoryAdapter;
    private ServantCardAdapter servantCardAdapter, unlockedServantCardAdapter, recommendedServantCardAdapter;
    private List<CategoryModel> categoryList;
    private List<ServantCardModel> servantList, unlockedServantList, allServants, recommendedServantList;
    private ProgressDialog progressDialog;

    private RecyclerView paymentHistoryRecyclerView;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<PaymentHistoryModel> paymentHistoryList;
    private TextView noPaymentHistoryText;
    private EditText tvName, mobile, aadharNumber;

    private ImageView updateProfilePicture, mobile_verified_icon, profileMembershipBadge, profileSectionPic;
    private TextView profileName, profileMobile;
    private ImageButton btnEditImage;
    private com.google.android.material.button.MaterialButton updateBtn;
    private String currentImageUrl, originalUsername, originalMobile, originalAadhar, originalImageUrl;
    private boolean isProfileChanged = false;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;
    private static final int PERMISSION_REQUEST_CODE = 103;
    private final String IMGBB_API_KEY = "2b7a203099dc367c13140ecb2a20ea9e"; // Replace with your ImgBB API key
    private ProgressDialog imageUploadDialog;

    // Profile section views
    private LinearLayout manageProfileSection, changeEmailSection, changePasswordSection, privacyOptionsSection, deleteAccountSection, paymentHistorySection, membershipHistorySection, unlockedProfilesDetailsSection, helpSupportSection, termsConditionsSection;
    private ImageButton manageProfileBackButton, changeEmailBackButton, changePasswordBackButton, privacyOptionsBackButton, deleteAccountBackButton, paymentHistoryBackButton, membershipHistoryBackButton, unlockedProfilesDetailsBackButton, helpSupportBackButton, termsConditionsBackButton;
    private CardView profileCard, changeEmailCard, changePasswordCard, logoutButton, notificationsCard, darkModeCard, privacyOptionsCard, deleteAccountCard, paymentHistoryCard, primeMembershipCard, unlockedProfilesDetailsCard, helpSupportCard, termsConditionsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Initialize views
        initializeViews();
        // Initialize Firebase
        initializeFirebase();
        // Initialize data
        initializeData();
        // Setup listeners for navigation
        setupNavigationListeners();
        // Setup profile section navigation
        setupProfileSectionNavigation();
        // Setup profile change listeners
        setupProfileChangeListeners();

        // Load user profile
        loadUserProfile();

        // Setup update button listener
        updateBtn.setOnClickListener(v -> handleUpdateProfile());

        // Setup image edit button listener
        btnEditImage.setOnClickListener(v -> checkAndRequestPermissions());

        // Load initial data with completion tracking
        loadInitialData();

        unlockBtn.setOnClickListener(v -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = snapshot.child("email").getValue(String.class);

                    Intent intent = new Intent(UserDashboard.this, PrimePaymentPortal.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("user_name", username != null ? username : "SAMPLE NAME");
                    intent.putExtra("user_email", email != null ? email : "example@gmail.com");
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserDashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        });

        imageUnlockBtn.setOnClickListener(v -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = snapshot.child("email").getValue(String.class);

                    Intent intent = new Intent(UserDashboard.this, PrimePaymentPortal.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("user_name", username != null ? username : "SAMPLE NAME");
                    intent.putExtra("user_email", email != null ? email : "example@gmail.com");
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserDashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        });

        layoutUnlockBtn.setOnClickListener(v -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = snapshot.child("email").getValue(String.class);

                    Intent intent = new Intent(UserDashboard.this, PrimePaymentPortal.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("user_name", username != null ? username : "SAMPLE NAME");
                    intent.putExtra("user_email", email != null ? email : "example@gmail.com");
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserDashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        });

        editFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterServants();
            }
        });

        String[] sortOptions = {"Most Relevant", "Experience ↑", "Experience ↓", "Salary ↑", "Salary ↓"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterServants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fabQuickHelp.setOnClickListener(v -> {
            fabQuickHelp.setVisibility(View.GONE);
            activateSearchSection();
            loadUrgentServants();
        });
    }

    private void initializeViews() {
        // Initialize dashboard views
        greet = findViewById(R.id.greet);
        heading = findViewById(R.id.heading);
        subheading = findViewById(R.id.subheading);
        popularServicesText = findViewById(R.id.popularServicesText);
        recommendText = findViewById(R.id.recommendText);
        profilePic = findViewById(R.id.profilePic);
        filterSpinnerArrow = findViewById(R.id.filterSpinnerArrow);
        membership_button = findViewById(R.id.membership_button);
        sortSpinnerArrow = findViewById(R.id.sortSpinnerArrow);
        unlockBtn = findViewById(R.id.unlockBtn);
        homeSectionButton = findViewById(R.id.homeSectionButton);
        searchSectionButton = findViewById(R.id.searchSectionButton);
        membershipSectionButton = findViewById(R.id.membershipSectionButton);
        unlockSectionButton = findViewById(R.id.unlockSectionButton);
        profileSectionButton = findViewById(R.id.profileSectionButton);
        imageUnlockBtn = findViewById(R.id.imageUnlockBtn);
        filterSpinner = findViewById(R.id.filterSpinner);
        sortSpinner = findViewById(R.id.sortSpinner);
        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        recommendRecyclerView = findViewById(R.id.recommendRecyclerView);
        servantRecyclerView = findViewById(R.id.servantRecyclerView);
        servantUnlockedRecyclerView = findViewById(R.id.servantUnlockedRecyclerView);
        unlockedText = findViewById(R.id.unlockedText);
        searchSection = findViewById(R.id.searchSection);
        unlockedProfilesSection = findViewById(R.id.unlockedProfilesSection);
        profileSection = findViewById(R.id.profileSection);
        mainContentLayout = findViewById(R.id.mainContentLayout);
        layoutUnlockBtn = findViewById(R.id.layoutUnlockBtn);
        filterSortContainer = findViewById(R.id.filterSortContainer);
        membershipSection = findViewById(R.id.membershipSection);
        headerLayout = findViewById(R.id.headerLayout);
        homeSection = findViewById(R.id.homeSection);
        searchScrollView = findViewById(R.id.searchScrollView);
        unlockedContainer = findViewById(R.id.unlockedContainer);
        membershipScrollView = findViewById(R.id.membershipScrollView);
        profileScrollView = findViewById(R.id.profileScrollView);
        fabQuickHelp = findViewById(R.id.fabQuickHelp);
        editFilter = findViewById(R.id.editFilter);
        profileRootButtons = findViewById(R.id.profileRootButtons);
        btnViewProfile = findViewById(R.id.btnViewProfile);

        // Initialize profile section views
        manageProfileSection = findViewById(R.id.manageProfileSection);
        changeEmailSection = findViewById(R.id.changeEmailSection);
        changePasswordSection = findViewById(R.id.changePasswordSection);
        privacyOptionsSection = findViewById(R.id.privacyOptionsSection);
        deleteAccountSection = findViewById(R.id.deleteAccountSection);
        paymentHistorySection = findViewById(R.id.paymentHistorySection);
        membershipHistorySection = findViewById(R.id.membershipHistorySection);
        unlockedProfilesDetailsSection = findViewById(R.id.unlockedProfilesDetailsSection);
        helpSupportSection = findViewById(R.id.helpSupportSection);
        termsConditionsSection = findViewById(R.id.termsConditionsSection);

        manageProfileBackButton = findViewById(R.id.manageProfileBackButton);
        changeEmailBackButton = findViewById(R.id.changeEmailBackButton);
        changePasswordBackButton = findViewById(R.id.changePasswordBackButton);
        privacyOptionsBackButton = findViewById(R.id.privacyOptionsBackButton);
        deleteAccountBackButton = findViewById(R.id.deleteAccountBackButton);
        paymentHistoryBackButton = findViewById(R.id.paymentHistoryBackButton);
        membershipHistoryBackButton = findViewById(R.id.membershipHistoryBackButton);
        unlockedProfilesDetailsBackButton = findViewById(R.id.unlockedProfilesDetailsBackButton);
        helpSupportBackButton = findViewById(R.id.helpSupportBackButton);
        termsConditionsBackButton = findViewById(R.id.termsConditionsBackButton);

        tvName = findViewById(R.id.tvName);
        mobile = findViewById(R.id.mobile);
        aadharNumber = findViewById(R.id.aadhar_number);
        updateProfilePicture = findViewById(R.id.update_profile_picture);
        btnEditImage = findViewById(R.id.btn_edit_image);
        updateBtn = findViewById(R.id.update_btn);
        imageUploadDialog = new ProgressDialog(this);
        imageUploadDialog.setMessage("Uploading image...");
        imageUploadDialog.setCancelable(false);
        updateBtn.setVisibility(View.GONE);
        mobile_verified_icon = findViewById(R.id.mobile_verified_icon);
        profileMembershipBadge = findViewById(R.id.profileMembershipBadge);
        profileSectionPic = findViewById(R.id.profileSectionPhoto);
        profileName = findViewById(R.id.profileName);
        profileMobile = findViewById(R.id.profilePhone);

        profileCard = findViewById(R.id.profileCard);
        changeEmailCard = findViewById(R.id.changeEmailCard);
        changePasswordCard = findViewById(R.id.changePasswordCard);
        logoutButton = findViewById(R.id.logoutButton);
        notificationsCard = findViewById(R.id.notificationsCard);
        darkModeCard = findViewById(R.id.darkModeCard);
        privacyOptionsCard = findViewById(R.id.privacyOptionsCard);
        deleteAccountCard = findViewById(R.id.deleteAccountCard);
        paymentHistoryCard = findViewById(R.id.paymentHistoryCard);
        primeMembershipCard = findViewById(R.id.primeMembershipCard);
        unlockedProfilesDetailsCard = findViewById(R.id.unlockedProfilesDetailsCard);
        helpSupportCard = findViewById(R.id.helpSupportCard);
        termsConditionsCard = findViewById(R.id.termsConditionsCard);
        paymentHistoryRecyclerView = findViewById(R.id.paymentHistoryRecyclerView);
        noPaymentHistoryText = findViewById(R.id.noPaymentHistoryText);
        paymentHistoryList = new ArrayList<>();
        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentHistoryList);
        paymentHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentHistoryRecyclerView.setAdapter(paymentHistoryAdapter);

        // Set up RecyclerViews
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this, category -> {
            activateSearchSection();
            editFilter.setText(""); // Reset search query
            sortSpinner.setSelection(0); // Reset sort to "Most Relevant"
            for (int i = 0; i < filterSpinner.getAdapter().getCount(); i++) {
                if (filterSpinner.getAdapter().getItem(i).equals(category)) {
                    filterSpinner.setSelection(i);
                    break;
                }
            }
            filterServants();
        });
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        popularRecyclerView.setAdapter(categoryAdapter);

        servantList = new ArrayList<>();
        allServants = new ArrayList<>();
        servantCardAdapter = new ServantCardAdapter(servantList, this, (servantId, servantName) -> {
            checkProfileAccess(userId, servantId, servantName);
        });
        servantRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        servantRecyclerView.setAdapter(servantCardAdapter);

        recommendedServantList = new ArrayList<>();
        recommendedServantCardAdapter = new ServantCardAdapter(recommendedServantList, this, (servantId, servantName) -> {
            checkProfileAccess(userId, servantId, servantName);
        });
        recommendRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendRecyclerView.setAdapter(recommendedServantCardAdapter);

        unlockedServantList = new ArrayList<>();
        unlockedServantCardAdapter = new ServantCardAdapter(unlockedServantList, this, (servantId, servantName) -> {
            checkProfileAccess(userId, servantId, servantName);
        });
        servantUnlockedRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        servantUnlockedRecyclerView.setAdapter(unlockedServantCardAdapter);

        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    char firstChar = input.charAt(0);
                    if (firstChar < '6' || firstChar > '9') {
                        mobile.setError("Invalid mobile number.");
                    } else {
                        mobile.setError(null);
                    }
                }
                if (s.length() == 10 && s.toString().matches("^[6-9][0-9]{9}$")) {
                    mobile_verified_icon.setVisibility(View.VISIBLE);
                } else {
                    mobile_verified_icon.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkProfileChanges();
            }
        });

        aadharNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    char firstChar = input.charAt(0);
                    if (firstChar < '2') {
                        aadharNumber.setError("Invalid Aadhaar number.");
                    } else {
                        aadharNumber.setError(null);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkProfileChanges();
            }
        });
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        primeMembersRef = databaseReference.child("prime_members");
        usersRef = databaseReference.child("users");
        servantsRef = databaseReference.child("servants");
        ratingsRef = databaseReference.child("ratings");
        unlockedProfilesRef = databaseReference.child("unlocked_profiles");

        userId = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("username");
        if (userId == null || username == null) {
            Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeData() {
        activateHomeSection();
    }

    private void setupNavigationListeners() {
        homeSectionButton.setOnClickListener(v -> activateHomeSection());
        searchSectionButton.setOnClickListener(v -> {
            activateSearchSection();
            progressDialog.setMessage("Loading servants...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            loadSearchSectionServants(() -> {
                Log.d("SearchSection", "Servants loaded successfully!");
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                fabQuickHelp.setVisibility(View.VISIBLE);
            });
        });
        unlockSectionButton.setOnClickListener(v -> {
            activateUnlockedProfilesSection();
            progressDialog.setMessage("Loading servants...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            loadUnlockedServants(() -> {
                Log.d("SearchSection", "Servants loaded successfully!");
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            });
        });
        profileSectionButton.setOnClickListener(v -> activateProfileSection());
        membershipSectionButton.setOnClickListener(v -> {
            setupMembershipSection();
            activateMembershipSection();
        });
        btnViewProfile.setOnClickListener(v -> {
            activateSearchSection();
            progressDialog.setMessage("Loading servants...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            loadSearchSectionServants(() -> {
                Log.d("SearchSection", "Servants loaded successfully!");
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                fabQuickHelp.setVisibility(View.VISIBLE);
            });
        });
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void setupProfileSectionNavigation() {
        // Function to manage profile section visibility
        View.OnClickListener sectionClickListener = v -> {
            // Hide all sections and show profileRootButtons by default
            profileRootButtons.setVisibility(View.GONE);
            manageProfileSection.setVisibility(View.GONE);
            changeEmailSection.setVisibility(View.GONE);
            changePasswordSection.setVisibility(View.GONE);
            privacyOptionsSection.setVisibility(View.GONE);
            deleteAccountSection.setVisibility(View.GONE);
            paymentHistorySection.setVisibility(View.GONE);
            membershipHistorySection.setVisibility(View.GONE);
            unlockedProfilesDetailsSection.setVisibility(View.GONE);
            helpSupportSection.setVisibility(View.GONE);
            termsConditionsSection.setVisibility(View.GONE);

            // Show the corresponding section based on the clicked card
            if (v.getId() == R.id.profileCard) {
                manageProfileSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.changeEmailCard) {
                changeEmailSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.changePasswordCard) {
                changePasswordSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.privacyOptionsCard) {
                privacyOptionsSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.deleteAccountCard) {
                deleteAccountSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.paymentHistoryCard) {
                paymentHistorySection.setVisibility(View.VISIBLE);
                loadPaymentHistory();
            } else if (v.getId() == R.id.primeMembershipCard) {
                membershipHistorySection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.unlockedProfilesDetailsCard) {
                unlockedProfilesDetailsSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.helpSupportCard) {
                helpSupportSection.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.termsConditionsCard) {
                termsConditionsSection.setVisibility(View.VISIBLE);
            }
        };

        // Set click listeners for cards
        profileCard.setOnClickListener(sectionClickListener);
        changeEmailCard.setOnClickListener(sectionClickListener);
        changePasswordCard.setOnClickListener(sectionClickListener);
        privacyOptionsCard.setOnClickListener(sectionClickListener);
        deleteAccountCard.setOnClickListener(sectionClickListener);
        paymentHistoryCard.setOnClickListener(sectionClickListener);
        primeMembershipCard.setOnClickListener(sectionClickListener);
        unlockedProfilesDetailsCard.setOnClickListener(sectionClickListener);
        helpSupportCard.setOnClickListener(sectionClickListener);
        termsConditionsCard.setOnClickListener(sectionClickListener);

        // Common back button listener to return to profileRootButtons
        View.OnClickListener backButtonListener = v -> {
            // Hide all sections
            manageProfileSection.setVisibility(View.GONE);
            changeEmailSection.setVisibility(View.GONE);
            changePasswordSection.setVisibility(View.GONE);
            privacyOptionsSection.setVisibility(View.GONE);
            deleteAccountSection.setVisibility(View.GONE);
            paymentHistorySection.setVisibility(View.GONE);
            membershipHistorySection.setVisibility(View.GONE);
            unlockedProfilesDetailsSection.setVisibility(View.GONE);
            helpSupportSection.setVisibility(View.GONE);
            termsConditionsSection.setVisibility(View.GONE);
            // Show profileRootButtons
            profileRootButtons.setVisibility(View.VISIBLE);
            // Ensure profileSection is visible
            profileSection.setVisibility(View.VISIBLE);
        };

        // Set back button listeners
        manageProfileBackButton.setOnClickListener(backButtonListener);
        changeEmailBackButton.setOnClickListener(backButtonListener);
        changePasswordBackButton.setOnClickListener(backButtonListener);
        privacyOptionsBackButton.setOnClickListener(backButtonListener);
        deleteAccountBackButton.setOnClickListener(backButtonListener);
        paymentHistoryBackButton.setOnClickListener(backButtonListener);
        membershipHistoryBackButton.setOnClickListener(backButtonListener);
        unlockedProfilesDetailsBackButton.setOnClickListener(backButtonListener);
        helpSupportBackButton.setOnClickListener(backButtonListener);
        termsConditionsBackButton.setOnClickListener(backButtonListener);
    }

    private void showLogoutConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.logout)
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    Toast.makeText(this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                    new SessionManager(this).logout(this);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorDanger));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeGreeting = hour < 12 ? "Good Morning" : (hour < 17 ? "Good Afternoon" : "Good Evening");
        greet.setText(timeGreeting + ", " + username);
    }

    private void checkPrimeMembership(Runnable onComplete) {
        primeMembersRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isMember = false;

                        for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                            String endingDate = memberSnapshot.child("endingDate").getValue(String.class);
                            if (endingDate != null && isMembershipValid(endingDate)) {
                                isMember = true;
                                break;
                            }
                        }

                        membership_button.setVisibility(isMember ? View.VISIBLE : View.GONE);
                        profileMembershipBadge.setVisibility(isMember ? View.VISIBLE : View.GONE);
                        onComplete.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboard.this, "Error checking membership", Toast.LENGTH_SHORT).show();
                        onComplete.run();
                    }
                });
    }

    private boolean isMembershipValid(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date endDate = sdf.parse(dateStr);
            return endDate != null && endDate.after(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isNewer(String dateStr1, String dateStr2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date1 = sdf.parse(dateStr1);
            Date date2 = sdf.parse(dateStr2);
            return date1 != null && date2 != null && date1.after(date2);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadProfileImage(Runnable onComplete) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profilePhotoUrl = snapshot.child("profile_photo").getValue(String.class);
                if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                    Glide.with(UserDashboard.this).load(profilePhotoUrl).into(profilePic);
                } else {
                    profilePic.setImageResource(R.drawable.default_profile);
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Error loading profile image", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadPopularServices(Runnable onComplete) {
        servantsRef.orderByChild("isVerified").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> categoryCount = new HashMap<>();
                for (DataSnapshot servantSnap : snapshot.getChildren()) {
                    String category = servantSnap.child("category").getValue(String.class);
                    if (category != null) {
                        categoryCount.merge(category, 1, Integer::sum);
                    }
                }
                categoryList.clear();
                categoryCount.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .limit(4)
                        .forEach(entry -> {
                            int iconResId;
                            int backgroundColor;
                            switch (entry.getKey()) {
                                case "Cook":
                                    iconResId = R.drawable.cook_icon;
                                    backgroundColor = Color.parseColor("#FFECB3");
                                    break;
                                case "Maid":
                                    iconResId = R.drawable.maid_icon;
                                    backgroundColor = Color.parseColor("#B3E5FC");
                                    break;
                                case "Driver":
                                    iconResId = R.drawable.driver_icon;
                                    backgroundColor = Color.parseColor("#C8E6C9");
                                    break;
                                case "Baby Sitter":
                                    iconResId = R.drawable.babysitter_icon;
                                    backgroundColor = Color.parseColor("#F8BBD0");
                                    break;
                                default:
                                    iconResId = R.drawable.default_service_icon;
                                    backgroundColor = Color.parseColor("#E0E0E0");
                                    break;
                            }
                            categoryList.add(new CategoryModel(entry.getKey(), iconResId, backgroundColor));
                        });
                categoryAdapter.notifyDataSetChanged();
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Error loading popular services", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadRecommendedServants(Runnable onComplete) {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingsSnapshot) {
                Map<String, List<Double>> ratingMap = new HashMap<>();

                for (DataSnapshot ratingSnap : ratingsSnapshot.getChildren()) {
                    String servantId = ratingSnap.child("servantId").getValue(String.class);
                    Double rating = ratingSnap.child("rating").getValue(Double.class);
                    if (servantId != null && rating != null) {
                        ratingMap.putIfAbsent(servantId, new ArrayList<>());
                        ratingMap.get(servantId).add(rating);
                    }
                }

                servantsRef.orderByChild("isVerified").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recommendedServantList.clear();

                        for (DataSnapshot servantSnap : snapshot.getChildren()) {
                            Boolean isVerified = servantSnap.child("isVerified").getValue(Boolean.class);
                            String availability = servantSnap.child("availability").getValue(String.class);

                            if (Boolean.TRUE.equals(isVerified) && "Yes".equals(availability)) {
                                String servantId = servantSnap.getKey();
                                String name = servantSnap.child("name").getValue(String.class);
                                String category = servantSnap.child("category").getValue(String.class);
                                String experience = servantSnap.child("experience").getValue(String.class);
                                String area = servantSnap.child("area").getValue(String.class);
                                String expectedSalary = servantSnap.child("expectedSalary").getValue(String.class);
                                String profilePhoto = servantSnap.child("profilePhoto").getValue(String.class);

                                double avgRating = 0.0;
                                List<Double> ratings = ratingMap.get(servantId);
                                if (ratings != null && !ratings.isEmpty()) {
                                    double total = 0.0;
                                    for (Double r : ratings) total += r;
                                    avgRating = total / ratings.size();
                                }

                                if (name != null && category != null) {
                                    recommendedServantList.add(new ServantCardModel(
                                            servantId, name, category, experience, area,
                                            expectedSalary, avgRating, availability, profilePhoto, isVerified, null
                                    ));
                                }
                            }
                        }

                        // Sort by avgRating DESC
                        recommendedServantList.sort((s1, s2) -> Double.compare(s2.getAvgRating(), s1.getAvgRating()));

                        // Shuffle only if more than 10 and ratings are same
                        if (recommendedServantList.size() > 10) {
                            List<ServantCardModel> topRated = recommendedServantList.subList(0, 10);
                            recommendedServantList = new ArrayList<>(topRated);
                        }

                        runOnUiThread(() -> {
                            recommendedServantCardAdapter.notifyDataSetChanged();
                            recommendRecyclerView.invalidate();
                        });

                        onComplete.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboard.this, "Error loading recommended servants", Toast.LENGTH_SHORT).show();
                        onComplete.run();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadSearchSectionServants(Runnable onComplete) {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingsSnapshot) {
                Map<String, List<Double>> ratingMap = new HashMap<>();

                for (DataSnapshot ratingSnap : ratingsSnapshot.getChildren()) {
                    String servantId = ratingSnap.child("servantId").getValue(String.class);
                    Double rating = ratingSnap.child("rating").getValue(Double.class);
                    if (servantId != null && rating != null) {
                        ratingMap.putIfAbsent(servantId, new ArrayList<>());
                        ratingMap.get(servantId).add(rating);
                    }
                }

                servantsRef.orderByChild("createdAt").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allServants.clear();
                        servantList.clear();
                        Set<String> categorySet = new HashSet<>();

                        for (DataSnapshot servantSnap : snapshot.getChildren()) {
                            Boolean isVerified = servantSnap.child("isVerified").getValue(Boolean.class);
                            String availability = servantSnap.child("availability").getValue(String.class);
                            String createdAt = servantSnap.child("createdAt").getValue(String.class);

                            if (Boolean.TRUE.equals(isVerified) && "Yes".equalsIgnoreCase(availability)) {
                                String servantId = servantSnap.getKey();
                                String name = servantSnap.child("name").getValue(String.class);
                                String category = servantSnap.child("category").getValue(String.class);
                                String experience = servantSnap.child("experience").getValue(String.class);
                                String area = servantSnap.child("area").getValue(String.class);
                                String expectedSalary = servantSnap.child("expectedSalary").getValue(String.class);
                                String profilePhoto = servantSnap.child("profilePhoto").getValue(String.class);

                                double avgRating = 0.0;
                                List<Double> ratings = ratingMap.get(servantId);
                                if (ratings != null && !ratings.isEmpty()) {
                                    double total = 0.0;
                                    for (Double r : ratings) total += r;
                                    avgRating = total / ratings.size();
                                }

                                ServantCardModel model = new ServantCardModel(
                                        servantId, name, category, experience, area,
                                        expectedSalary, avgRating, availability, profilePhoto, isVerified, null
                                );
                                model.setCreatedAt(createdAt);
                                allServants.add(model);
                                categorySet.add(category);
                            }
                        }

                        allServants.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
                        servantList.addAll(allServants);
                        servantCardAdapter.notifyDataSetChanged();
                        setupCategoryFilterSpinner(new ArrayList<>(categorySet));
                        onComplete.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboard.this, "Failed to load servants", Toast.LENGTH_SHORT).show();
                        onComplete.run();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void loadUnlockedServants(Runnable onComplete) {
        DatabaseReference unlockedProfilesRef = FirebaseDatabase.getInstance().getReference("unlocked_profiles");
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingsSnapshot) {
                Map<String, List<Double>> ratingMap = new HashMap<>();

                for (DataSnapshot ratingSnap : ratingsSnapshot.getChildren()) {
                    String servantId = ratingSnap.child("servantId").getValue(String.class);
                    Double rating = ratingSnap.child("rating").getValue(Double.class);
                    if (servantId != null && rating != null) {
                        ratingMap.putIfAbsent(servantId, new ArrayList<>());
                        ratingMap.get(servantId).add(rating);
                    }
                }

                unlockedProfilesRef.orderByChild("userId").equalTo(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<String> validServantIds = new ArrayList<>();

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date currentDate = new Date();

                                for (DataSnapshot unlockSnap : snapshot.getChildren()) {
                                    String servantId = unlockSnap.child("servantId").getValue(String.class);
                                    String unlockedTill = unlockSnap.child("unlockedTill").getValue(String.class);

                                    try {
                                        if (servantId != null) {
                                            if ("Lifetime".equalsIgnoreCase(unlockedTill)) {
                                                validServantIds.add(servantId);
                                            } else {
                                                Date expiryDate = sdf.parse(unlockedTill);
                                                if (expiryDate != null && !currentDate.after(expiryDate)) {
                                                    validServantIds.add(servantId);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (validServantIds.isEmpty()) {
                                    unlockedText.setVisibility(View.VISIBLE);
                                    servantUnlockedRecyclerView.setVisibility(View.GONE);
                                    onComplete.run();
                                    return;
                                }

                                fetchServantsByIds(validServantIds, ratingMap, onComplete);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UserDashboard.this, "Failed to load unlocked profiles", Toast.LENGTH_SHORT).show();
                                onComplete.run();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void fetchServantsByIds(List<String> servantIds, Map<String, List<Double>> ratingMap, Runnable onComplete) {
        DatabaseReference servantsRef = FirebaseDatabase.getInstance().getReference("servants");

        servantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ServantCardModel> unlockedServants = new ArrayList<>();

                for (String servantId : servantIds) {
                    if (snapshot.hasChild(servantId)) {
                        DataSnapshot servantSnap = snapshot.child(servantId);

                        String name = servantSnap.child("name").getValue(String.class);
                        String category = servantSnap.child("category").getValue(String.class);
                        String experience = servantSnap.child("experience").getValue(String.class);
                        String area = servantSnap.child("area").getValue(String.class);
                        String expectedSalary = servantSnap.child("expectedSalary").getValue(String.class);
                        String profilePhoto = servantSnap.child("profilePhoto").getValue(String.class);

                        double avgRating = 0.0;
                        List<Double> ratings = ratingMap.get(servantId);
                        if (ratings != null && !ratings.isEmpty()) {
                            double total = 0.0;
                            for (Double r : ratings) total += r;
                            avgRating = total / ratings.size();
                        }

                        ServantCardModel model = new ServantCardModel(
                                servantId, name, category, experience, area,
                                expectedSalary, avgRating, "Yes", profilePhoto, true, null
                        );
                        unlockedServants.add(model);
                    }
                }

                unlockedServantList.clear();
                unlockedServantList.addAll(unlockedServants);
                unlockedServantCardAdapter.notifyDataSetChanged();

                if (unlockedServants.isEmpty()) {
                    unlockedText.setVisibility(View.VISIBLE);
                    servantUnlockedRecyclerView.setVisibility(View.GONE);
                } else {
                    unlockedText.setVisibility(View.GONE);
                    servantUnlockedRecyclerView.setVisibility(View.VISIBLE);
                }

                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to fetch servant data", Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void setupCategoryFilterSpinner(List<String> categories) {
        Collections.sort(categories);
        categories.add(0, "All Categories");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterServants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterServants() {
        String query = editFilter.getText().toString().toLowerCase();
        String selectedCategory = filterSpinner.getSelectedItem().toString();
        String sortOption = sortSpinner.getSelectedItem().toString();

        List<ServantCardModel> filtered = new ArrayList<>();
        for (ServantCardModel servant : allServants) {
            String name = servant.getName() != null ? servant.getName().toLowerCase() : "";
            String area = servant.getArea() != null ? servant.getArea().toLowerCase() : "";
            boolean matchesQuery = query.isEmpty() || name.contains(query) || area.contains(query);
            boolean matchesCategory = selectedCategory.equals("All Categories") || servant.getCategory().equalsIgnoreCase(selectedCategory);

            if (matchesQuery && matchesCategory) {
                filtered.add(servant);
            }
        }

        switch (sortOption) {
            case "Experience ↑":
                filtered.sort(Comparator.comparingInt(s -> Integer.parseInt(s.getExperience())));
                break;
            case "Experience ↓":
                filtered.sort((s1, s2) -> Integer.parseInt(s2.getExperience()) - Integer.parseInt(s1.getExperience()));
                break;
            case "Salary ↑":
                filtered.sort(Comparator.comparingInt(s -> Integer.parseInt(s.getExpectedSalary())));
                break;
            case "Salary ↓":
                filtered.sort((s1, s2) -> Integer.parseInt(s2.getExpectedSalary()) - Integer.parseInt(s1.getExpectedSalary()));
                break;
            case "Most Relevant":
            default:
                filtered.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
                break;
        }

        servantList.clear();
        servantList.addAll(filtered);
        servantCardAdapter.notifyDataSetChanged();
    }

    private void checkProfileAccess(String userId, String servantId, String servantName) {
        if (userId == null || servantId == null || userId.isEmpty() || servantId.isEmpty()) {
            Toast.makeText(this, "Invalid user or servant ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        unlockedProfilesRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isUnlocked = false;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String dbServantId = snap.child("servantId").getValue(String.class);
                            if (servantId.equals(dbServantId)) {
                                isUnlocked = true;
                                break;
                            }
                        }

                        if (isUnlocked) {
                            openProfile(userId, servantId, servantName);
                        } else {
                            primeMembersRef.orderByChild("userId").equalTo(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            boolean hasActiveMembership = false;
                                            DataSnapshot latestSnapshot = null;

                                            for (DataSnapshot memberSnap : snapshot.getChildren()) {
                                                String endingDate = memberSnap.child("endingDate").getValue(String.class);
                                                if (endingDate != null && isMembershipValid(endingDate)) {
                                                    if (latestSnapshot == null || isNewer(endingDate, latestSnapshot.child("endingDate").getValue(String.class))) {
                                                        latestSnapshot = memberSnap;
                                                    }
                                                }
                                            }

                                            if (latestSnapshot != null) {
                                                hasActiveMembership = true;
                                                String endingDate = latestSnapshot.child("endingDate").getValue(String.class);
                                                String unlockedNum = "UP" + getRandomAlphaNumeric(3);

                                                DatabaseReference unlockedRef = FirebaseDatabase.getInstance()
                                                        .getReference("unlocked_profiles").child(unlockedNum);

                                                Map<String, Object> data = new HashMap<>();
                                                data.put("servantId", servantId);
                                                data.put("servantName", servantName);
                                                data.put("unlockedNum", unlockedNum);
                                                data.put("unlockedTill", endingDate);
                                                data.put("unlockedWay", "through Prime Membership");
                                                data.put("userId", userId);

                                                unlockedRef.setValue(data).addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        openProfile(userId, servantId, servantName);
                                                    }
                                                });
                                            }

                                            if (!hasActiveMembership) {
                                                showBlurredUnlockDialog(servantId, servantName, userId);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(UserDashboard.this, "Error checking membership status", Toast.LENGTH_SHORT).show();
                                            showBlurredUnlockDialog(servantId, servantName, userId);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboard.this, "Error checking unlocked profiles", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void openProfile(String userId, String servantId, String servantName) {
        Intent intent = new Intent(UserDashboard.this, ServantProfile.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("username", username);
        intent.putExtra("servant_id", servantId);
        intent.putExtra("servant_name", servantName);
        startActivity(intent);
    }

    private void showBlurredUnlockDialog(String servantId, String servantName, String userId) {
        Dialog dialog = new Dialog(this, R.style.CompactDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_unlock_profile, null);

        TextView servantTitle = view.findViewById(R.id.dialog_servant_name);
        TextView subText = view.findViewById(R.id.dialog_subtext);
        Button unlockBtn = view.findViewById(R.id.dialog_unlock_btn);
        Button cancelBtn = view.findViewById(R.id.dialog_cancel_btn);

        servantTitle.setText("Unlock " + servantName + "'s profile?");
        subText.setText("To view more about " + servantName + ",\n unlock their profile for just ₹20.");

        unlockBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(UserDashboard.this, PaymentPortal.class);
            intent.putExtra("servant_id", servantId);
            intent.putExtra("servant_name", servantName);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void loadUrgentServants() {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading urgent help...");
        progressDialog.show();

        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingsSnapshot) {
                // Step 1: Build rating map (servantId -> List of ratings)
                Map<String, List<Double>> ratingMap = new HashMap<>();
                for (DataSnapshot ratingSnap : ratingsSnapshot.getChildren()) {
                    String servantId = ratingSnap.child("servantId").getValue(String.class);
                    Double rating = ratingSnap.child("rating").getValue(Double.class);
                    if (servantId != null && rating != null) {
                        ratingMap.putIfAbsent(servantId, new ArrayList<>());
                        ratingMap.get(servantId).add(rating);
                    }
                }

                // Step 2: Load urgent servants
                servantsRef.orderByChild("createdAt").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allServants.clear();
                        servantList.clear();
                        Set<String> categorySet = new HashSet<>();

                        for (DataSnapshot servantSnap : snapshot.getChildren()) {
                            Boolean isVerified = servantSnap.child("isVerified").getValue(Boolean.class);
                            String availability = servantSnap.child("availability").getValue(String.class);
                            String createdAt = servantSnap.child("createdAt").getValue(String.class);
                            Object urgentObj = servantSnap.child("urgentCharge").getValue();
                            String urgentCharge = urgentObj != null ? urgentObj.toString() : "Not Available";

                            if (Boolean.TRUE.equals(isVerified) && "Yes".equalsIgnoreCase(availability)
                                    && urgentCharge != null && !urgentCharge.equalsIgnoreCase("Not Available")) {

                                String servantId = servantSnap.getKey();
                                String name = servantSnap.child("name").getValue(String.class);
                                String category = servantSnap.child("category").getValue(String.class);
                                String experience = servantSnap.child("experience").getValue(String.class);
                                String area = servantSnap.child("area").getValue(String.class);
                                String profilePhoto = servantSnap.child("profilePhoto").getValue(String.class);

                                // Step 3: Compute average rating
                                double avgRating = 0.0;
                                List<Double> servantRatings = ratingMap.get(servantId);
                                if (servantRatings != null && !servantRatings.isEmpty()) {
                                    double sum = 0.0;
                                    for (double r : servantRatings) sum += r;
                                    avgRating = sum / servantRatings.size();
                                }

                                ServantCardModel model = new ServantCardModel(
                                        servantId, name, category, experience, area, null,
                                        avgRating, availability, profilePhoto, isVerified, urgentCharge
                                );
                                model.setCreatedAt(createdAt);

                                allServants.add(model);
                                categorySet.add(category);
                            }
                        }

                        allServants.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
                        servantList.addAll(allServants);
                        servantCardAdapter.notifyDataSetChanged();
                        setupCategoryFilterSpinner(new ArrayList<>(categorySet));

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboard.this, "Failed to load urgent servants", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void setupMembershipSection() {
        LinearLayout primePurchasedCard = findViewById(R.id.primePurchasedCard);
        LinearLayout primeCard = findViewById(R.id.primeCard);
        TextView primeMemberName = findViewById(R.id.primeMemberName);
        TextView primeMemberEmail = findViewById(R.id.primeMemberEmail);
        TextView primeMemberNumber = findViewById(R.id.primeMemberNumber);
        TextView primeEndDate = findViewById(R.id.primeEndDate);
        com.google.android.material.button.MaterialButton btnJoinNow = findViewById(R.id.btnJoinNow);
        com.google.android.material.button.MaterialButton btnRenewNow = findViewById(R.id.btnRenewNow);
        de.hdodenhof.circleimageview.CircleImageView primeMemberPhoto = findViewById(R.id.primeMemberPhoto);

        primeMembersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasActiveMembership = false;
                DataSnapshot latestSnapshot = null;

                for (DataSnapshot primeSnapshot : snapshot.getChildren()) {
                    String endDateStr = primeSnapshot.child("endingDate").getValue(String.class);
                    if (endDateStr != null && isMembershipValid(endDateStr)) {
                        if (latestSnapshot == null || isNewer(endDateStr, latestSnapshot.child("endingDate").getValue(String.class))) {
                            latestSnapshot = primeSnapshot;
                        }
                    }
                }

                if (latestSnapshot != null) {
                    hasActiveMembership = true;
                    String endDateStr = latestSnapshot.child("endingDate").getValue(String.class);
                    String memberNumber = latestSnapshot.child("primeNumber").getValue(String.class);

                    primePurchasedCard.setVisibility(View.VISIBLE);
                    primeCard.setVisibility(View.GONE);

                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String name = userSnapshot.child("username").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);
                            String profilePhotoUrl = userSnapshot.child("profile_photo").getValue(String.class);

                            primeMemberName.setText(name != null ? name : "SAMPLE NAME");
                            primeMemberEmail.setText(email != null ? email : "example@gmail.com");

                            if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                                Glide.with(UserDashboard.this)
                                        .load(profilePhotoUrl)
                                        .error(R.drawable.default_profile)
                                        .into(primeMemberPhoto);
                            } else {
                                primeMemberPhoto.setImageResource(R.drawable.default_profile);
                            }

                            if (memberNumber != null && memberNumber.length() == 16) {
                                String formattedNumber = memberNumber.substring(0, 4) + " " +
                                        memberNumber.substring(4, 8) + " " +
                                        memberNumber.substring(8, 12) + " " +
                                        memberNumber.substring(12, 16);
                                primeMemberNumber.setText(formattedNumber);
                            } else {
                                primeMemberNumber.setText("1234 1234 1234 1234");
                            }

                            primeEndDate.setText("Ending On: " + (endDateStr != null ? endDateStr : "DD/MM/YYYY"));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UserDashboard.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (!hasActiveMembership) {
                    primePurchasedCard.setVisibility(View.GONE);
                    primeCard.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Error checking membership status", Toast.LENGTH_SHORT).show();
                primePurchasedCard.setVisibility(View.GONE);
                primeCard.setVisibility(View.VISIBLE);
            }
        });

        btnJoinNow.setOnClickListener(v -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = snapshot.child("email").getValue(String.class);

                    Intent intent = new Intent(UserDashboard.this, PrimePaymentPortal.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("user_name", username != null ? username : "SAMPLE NAME");
                    intent.putExtra("user_email", email != null ? email : "example@gmail.com");
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserDashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnRenewNow.setOnClickListener(v -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = snapshot.child("email").getValue(String.class);

                    Intent intent = new Intent(UserDashboard.this, PrimePaymentPortal.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("user_name", username != null ? username : "SAMPLE NAME");
                    intent.putExtra("user_email", email != null ? email : "example@gmail.com");
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserDashboard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void activateHomeSection() {
        homeSection.setVisibility(View.VISIBLE);
        searchSection.setVisibility(View.GONE);
        unlockedProfilesSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.GONE);
        membershipSection.setVisibility(View.GONE);
        fabQuickHelp.setVisibility(View.VISIBLE);
        homeSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        homeSectionButton.setImageResource(R.drawable.home_icon_active);
        searchSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        searchSectionButton.setImageResource(R.drawable.search_icon);
        unlockSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        unlockSectionButton.setImageResource(R.drawable.unlocked_profiles);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profiles);
        membershipSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        membershipSectionButton.setImageResource(R.drawable.membership_icon);
    }

    private void activateSearchSection() {
        homeSection.setVisibility(View.GONE);
        searchSection.setVisibility(View.VISIBLE);
        unlockedProfilesSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.GONE);
        membershipSection.setVisibility(View.GONE);
        searchSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        searchSectionButton.setImageResource(R.drawable.search_active);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon);
        unlockSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        unlockSectionButton.setImageResource(R.drawable.unlocked_profiles);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profiles);
        membershipSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        membershipSectionButton.setImageResource(R.drawable.membership_icon);
    }

    private void activateUnlockedProfilesSection() {
        homeSection.setVisibility(View.GONE);
        searchSection.setVisibility(View.GONE);
        unlockedProfilesSection.setVisibility(View.VISIBLE);
        profileSection.setVisibility(View.GONE);
        membershipSection.setVisibility(View.GONE);
        fabQuickHelp.setVisibility(View.VISIBLE);
        unlockSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        unlockSectionButton.setImageResource(R.drawable.unlocked_profiles_active);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon);
        searchSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        searchSectionButton.setImageResource(R.drawable.search_icon);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profiles);
        membershipSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        membershipSectionButton.setImageResource(R.drawable.membership_icon);
    }

    private void activateProfileSection() {
        homeSection.setVisibility(View.GONE);
        searchSection.setVisibility(View.GONE);
        unlockedProfilesSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.VISIBLE);
        profileRootButtons.setVisibility(View.VISIBLE);
        membershipSection.setVisibility(View.GONE);
        fabQuickHelp.setVisibility(View.GONE);
        profileSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        profileSectionButton.setImageResource(R.drawable.profiles_active);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon);
        searchSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        searchSectionButton.setImageResource(R.drawable.search_icon);
        unlockSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        unlockSectionButton.setImageResource(R.drawable.unlocked_profiles);
        membershipSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        membershipSectionButton.setImageResource(R.drawable.membership_icon);

        // Ensure all sub-sections are hidden initially
        manageProfileSection.setVisibility(View.GONE);
        changeEmailSection.setVisibility(View.GONE);
        changePasswordSection.setVisibility(View.GONE);
        privacyOptionsSection.setVisibility(View.GONE);
        deleteAccountSection.setVisibility(View.GONE);
        paymentHistorySection.setVisibility(View.GONE);
        membershipHistorySection.setVisibility(View.GONE);
        unlockedProfilesDetailsSection.setVisibility(View.GONE);
        helpSupportSection.setVisibility(View.GONE);
        termsConditionsSection.setVisibility(View.GONE);
        profileRootButtons.setVisibility(View.VISIBLE);
    }

    private void activateMembershipSection() {
        homeSection.setVisibility(View.GONE);
        searchSection.setVisibility(View.GONE);
        unlockedProfilesSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.GONE);
        membershipSection.setVisibility(View.VISIBLE);
        fabQuickHelp.setVisibility(View.GONE);
        membershipSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        membershipSectionButton.setImageResource(R.drawable.membership_icon_active);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profiles);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon);
        searchSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        searchSectionButton.setImageResource(R.drawable.search_icon);
        unlockSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        unlockSectionButton.setImageResource(R.drawable.unlocked_profiles);
    }

    private void loadPaymentHistory() {
        DatabaseReference paymentRef = FirebaseDatabase.getInstance().getReference("unlock_payments");
        paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentHistoryList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot paymentSnap : snapshot.getChildren()) {
                        String fetchedUserId = paymentSnap.child("userId").getValue(String.class);
                        if (fetchedUserId != null && fetchedUserId.equals(userId)) {
                            String transactionID = paymentSnap.child("transactionID").getValue(String.class);
                            String email = paymentSnap.child("userEmail").getValue(String.class);
                            String amount = paymentSnap.child("paid_amount").getValue(String.class);
                            String date = paymentSnap.child("paymentDate").getValue(String.class);

                            if (transactionID != null && email != null && amount != null && date != null) {
                                paymentHistoryList.add(new PaymentHistoryModel(transactionID, email, amount, date, userId));
                            }
                        }
                    }

                    paymentHistoryAdapter.notifyDataSetChanged();
                    paymentHistoryRecyclerView.setVisibility(View.VISIBLE);
                    noPaymentHistoryText.setVisibility(paymentHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    paymentHistoryRecyclerView.setVisibility(View.GONE);
                    noPaymentHistoryText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Error loading payment history", Toast.LENGTH_SHORT).show();
                paymentHistoryRecyclerView.setVisibility(View.GONE);
                noPaymentHistoryText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadInitialData() {
        AtomicInteger tasksCompleted = new AtomicInteger(0);
        int totalTasks = 5;

        Runnable checkAllTasksCompleted = () -> {
            if (tasksCompleted.incrementAndGet() >= totalTasks) {
                progressDialog.dismiss();
            }
        };

        updateGreeting();
        checkAllTasksCompleted.run();

        checkPrimeMembership(checkAllTasksCompleted);
        loadProfileImage(checkAllTasksCompleted);
        loadPopularServices(checkAllTasksCompleted);
        loadRecommendedServants(checkAllTasksCompleted);
        loadSearchSectionServants(checkAllTasksCompleted);
    }

    private void setupProfileChangeListeners() {
        TextWatcher profileTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkProfileChanges();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        tvName.addTextChangedListener(profileTextWatcher);
        mobile.addTextChangedListener(profileTextWatcher);
        aadharNumber.addTextChangedListener(profileTextWatcher);
    }

    private void checkProfileChanges() {
        String currentName = tvName.getText().toString().trim();
        String currentMobile = mobile.getText().toString().trim();
        String currentAadhar = aadharNumber.getText().toString().trim();
        boolean hasChanges =
                !currentName.equals(originalUsername) ||
                        !currentMobile.equals(originalMobile) ||
                        !currentAadhar.equals(originalAadhar) ||
                        !Objects.equals(currentImageUrl, originalImageUrl);
        isProfileChanged = hasChanges;
        updateBtn.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
    }

    private void loadUserProfile() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    originalUsername = snapshot.child("username").getValue(String.class);
                    originalMobile = snapshot.child("mobile_number").getValue(String.class);
                    originalAadhar = snapshot.child("aadhaar").getValue(String.class);
                    originalImageUrl = snapshot.child("profile_photo").getValue(String.class);

                    tvName.setText(originalUsername);
                    profileName.setText(originalUsername);
                    mobile.setText(originalMobile);
                    profileMobile.setText(originalMobile);
                    aadharNumber.setText(originalAadhar);
                    if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
                        currentImageUrl = originalImageUrl;
                        RequestBuilder<Drawable> glideRequest = Glide.with(UserDashboard.this)
                                .load(originalImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile);

                        glideRequest.into(updateProfilePicture);
                        glideRequest.into(profileSectionPic);
                    } else {
                        currentImageUrl = null;
                        updateProfilePicture.setImageResource(R.drawable.default_profile);
                        profileSectionPic.setImageResource(R.drawable.default_profile);
                    }
                    checkProfileChanges();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateProfile() {
        String newName = tvName.getText().toString().trim();
        String newMobile = mobile.getText().toString().trim();
        String newAadhar = aadharNumber.getText().toString().trim();

        // Validate inputs
        if (newName.isEmpty() || newMobile.isEmpty() || newAadhar.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newName.matches("^[a-zA-Z\\s]+$")) {
            tvName.setError("Only alphabets allowed in name");
            return;
        }
        if (!newMobile.matches("^[6-9][0-9]{9}$")) {
            mobile.setError("Valid 10-digit mobile number is required");
            return;
        }
        if (!newAadhar.matches("^[2-9][0-9]{11}$")) {
            aadharNumber.setError("Valid 12-digit Aadhaar number is required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (!newName.equals(originalUsername)) {
            updates.put("username", newName);
        }
        if (!newMobile.equals(originalMobile)) {
            updates.put("mobile_number", newMobile);
        }
        if (!newAadhar.equals(originalAadhar)) {
            updates.put("aadhaar", newAadhar);
        }
        if (!Objects.equals(currentImageUrl, originalImageUrl)) {
            updates.put("profile_photo", currentImageUrl != null ? currentImageUrl : "");
        }

        if (!updates.isEmpty()) {
            usersRef.child(userId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserDashboard.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        originalUsername = newName;
                        originalMobile = newMobile;
                        originalAadhar = newAadhar;
                        originalImageUrl = currentImageUrl;
                        isProfileChanged = false;
                        updateBtn.setVisibility(View.GONE);
                        loadUserProfile();
                    })
                    .addOnFailureListener(e -> Toast.makeText(UserDashboard.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            showImageOptions();
        }
    }

    private void showImageOptions() {
        String[] options = {"Upload from Camera", "Upload from Gallery", "Delete Image"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    } else {
                        currentImageUrl = "";
                        updateProfilePicture.setImageResource(R.drawable.default_profile);
                        usersRef.child(userId).child("profile_photo").setValue("")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UserDashboard.this, "Profile image deleted", Toast.LENGTH_SHORT).show();
                                    checkProfileChanges();
                                })
                                .addOnFailureListener(e -> Toast.makeText(UserDashboard.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Gallery app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            } else {
                throw new IOException("Unable to open input stream for URI: " + uri);
            }
        }
    }

    private void uploadImageToImgBB(Bitmap bitmap) {
        imageUploadDialog.show();
        updateBtn.setEnabled(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                .addFormDataPart("key", IMGBB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    imageUploadDialog.dismiss();
                    updateBtn.setEnabled(true);
                    Toast.makeText(UserDashboard.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    imageUploadDialog.dismiss();
                    updateBtn.setEnabled(true);
                });
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            String newImageUrl = json.getJSONObject("data").getString("url");
                            runOnUiThread(() -> {
                                currentImageUrl = newImageUrl;
                                Glide.with(UserDashboard.this)
                                        .load(newImageUrl)
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .into(updateProfilePicture);
                                Toast.makeText(UserDashboard.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                checkProfileChanges();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(UserDashboard.this, "Image upload failed", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(UserDashboard.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(UserDashboard.this, "Image upload failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                showImageOptions();
            } else {
                Toast.makeText(this, "Required permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = null;
            if (requestCode == CAMERA_REQUEST_CODE) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    updateProfilePicture.setImageBitmap(bitmap);
                    uploadImageToImgBB(bitmap);
                } else {
                    Toast.makeText(this, "Failed to capture image from camera", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    Glide.with(this)
                            .load(selectedImage)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(updateProfilePicture);
                    bitmap = getBitmapFromUri(selectedImage);
                    if (bitmap != null) {
                        uploadImageToImgBB(bitmap);
                    } else {
                        Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No image selected from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}