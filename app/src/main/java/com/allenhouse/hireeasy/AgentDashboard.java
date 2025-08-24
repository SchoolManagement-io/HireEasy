package com.allenhouse.hireeasy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AgentDashboard extends BaseActivity {

    private EditText tvName;
    private EditText mobile;
    private EditText currentAddress;
    private EditText area;
    private EditText experience;
    private EditText aadharNumber;
    private EditText editFilter;
    private EditText editUsername;
    private EditText editEmail;
    private EditText editMobile;
    private EditText urgentCharge;
    private EditText editSalary;
    private ImageView profilePicture, mobileVerifiedIcon, agentProfilePicture;
    private Spinner categorySpinner, genderSpinner, availabilitySpinner, filterSpinner;
    private MaterialButton submitBtn, btnUpdateProfile, btnChangePassword;
    private ImageButton btnEditImage, btnEditProfileImage, homeSectionButton, registerSectionButton, profileSectionButton, logoutButton;
    private LinearLayout homeSection, registrationSection, profileSection;
    private RecyclerView recyclerViewServants;
    private CheckBox cbVerifyAadhar;
    private TextView tvTotalRegistration, tvVerified, tvUnverified, username_head, register_servant;
    private List<ServantRegistrationModel> servantList, filteredServantList;
    private ServantRegistrationAdapter adapter;
    private String agentId, username, editingServantId, currentImageUrl, originalUsername, originalMobile, originalAgentImageUrl;
    private DatabaseReference databaseReference, agentReference;
    private boolean isEditing = false;
    private boolean isProfileChanged = false;
    private static final int PAYMENT_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;
    private static final int PERMISSION_REQUEST_CODE = 103;
    private final String[] categoryOptions = {"Select category", "Cook", "Maid", "Driver", "Baby Sitter"};
    private final String[] genderOptions = {"Gender", "Male", "Female", "Other"};
    private final String[] availabilityOptions = {"Availability", "Yes", "No"};
    private final String[] filterOptions = {"All", "Verified", "Unverified"};
    private final String IMGBB_API_KEY = "2b7a203099dc367c13140ecb2a20ea9e";
    private ProgressDialog imageUploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agent_dashboard);

        // Get agent_id and username from Intent
        agentId = getIntent().getStringExtra("agent_id");
        username = getIntent().getStringExtra("username");
        if (agentId == null) {
            ToastUtil.error(this, "Agent ID not found!");
            finish();
            return;
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("servants");
        agentReference = FirebaseDatabase.getInstance().getReference().child("agents").child(agentId);

        // Initialize views
        initializeViews();
        setupSpinners();
        setupInputValidation();
        fetchServantsFromFirebase();
        loadAgentProfile();

        // Setup listeners
        homeSectionButton.setOnClickListener(v -> {
            activateHomeSection();
            applyButtonAnimation(v);
        });
        registerSectionButton.setOnClickListener(v -> {
            activateRegistrationSection();
            clearForm();
            applyButtonAnimation(v);
        });
        profileSectionButton.setOnClickListener(v -> {
            activateProfileSection();
            applyButtonAnimation(v);
        });
        logoutButton.setOnClickListener(v -> {
            applyButtonAnimation(v);
            showLogoutConfirmation();
        });

        btnEditImage.setOnClickListener(v -> {
            applyButtonAnimation(v);
            checkAndRequestPermissions(true);
        });
        btnEditProfileImage.setOnClickListener(v -> {
            applyButtonAnimation(v);
            checkAndRequestPermissions(false);
        });
        submitBtn.setOnClickListener(v -> {
            applyButtonAnimation(v);
            handleSubmit();
        });
        btnUpdateProfile.setOnClickListener(v -> {
            applyButtonAnimation(v);
            handleUpdateProfile();
        });
        btnChangePassword.setOnClickListener(v -> {
            applyButtonAnimation(v);
            showChangePasswordDialog();
        });

        editFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServants(s.toString(), filterSpinner.getSelectedItem().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterServants(editFilter.getText().toString(), filterOptions[position]);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Setup profile change listeners
        setupProfileChangeListeners();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.profile_picture);
        btnEditImage = findViewById(R.id.btn_edit_image);
        tvName = findViewById(R.id.tvName);
        mobile = findViewById(R.id.mobile);
        mobileVerifiedIcon = findViewById(R.id.mobile_verified_icon);
        experience = findViewById(R.id.experience);
        currentAddress = findViewById(R.id.current_address);
        area = findViewById(R.id.area);
        aadharNumber = findViewById(R.id.aadhar_number);
        categorySpinner = findViewById(R.id.category);
        genderSpinner = findViewById(R.id.gender);
        availabilitySpinner = findViewById(R.id.availability);
        submitBtn = findViewById(R.id.submit_btn);
        editFilter = findViewById(R.id.editFilter);
        filterSpinner = findViewById(R.id.filterSpinner);
        tvTotalRegistration = findViewById(R.id.tvTotalRegistration);
        tvVerified = findViewById(R.id.tvVerified);
        tvUnverified = findViewById(R.id.tvUnverified);
        homeSection = findViewById(R.id.homeSection);
        registrationSection = findViewById(R.id.registrationSection);
        profileSection = findViewById(R.id.profileSection);
        homeSectionButton = findViewById(R.id.homeSectionButton);
        registerSectionButton = findViewById(R.id.registerSectionButton);
        profileSectionButton = findViewById(R.id.profileSectionButton);
        logoutButton = findViewById(R.id.logoutButton);
        recyclerViewServants = findViewById(R.id.recyclerViewServants);
        cbVerifyAadhar = findViewById(R.id.cb_verify_aadhar);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editMobile = findViewById(R.id.editMobile);
        agentProfilePicture = findViewById(R.id.agent_profile_picture);
        btnEditProfileImage = findViewById(R.id.btn_edit_profile_image);
        username_head = findViewById(R.id.username_head);
        register_servant = findViewById(R.id.register_servant);
        urgentCharge = findViewById(R.id.urgentCharge);
        editSalary = findViewById(R.id.editSalary);

        // Set email field as non-editable
        editEmail.setEnabled(false);
        editEmail.setKeyListener(null);

        // Initialize progress dialog for image upload
        imageUploadDialog = new ProgressDialog(this);
        imageUploadDialog.setMessage("Uploading image...");
        imageUploadDialog.setCancelable(false);

        // Set initial visibility of update profile button to GONE
        btnUpdateProfile.setVisibility(View.GONE);

        username_head.setText("Welcome " + username + " !");

        recyclerViewServants.setLayoutManager(new LinearLayoutManager(this));
        servantList = new ArrayList<>();
        filteredServantList = new ArrayList<>();
        adapter = new ServantRegistrationAdapter(filteredServantList,
                (model, position) -> {
                    isEditing = true;
                    register_servant.setText("Update Servant Profile");
                    editingServantId = model.getId();
                    populateFormForEdit(model);
                    submitBtn.setText("Update Servant");
                    submitBtn.setTextColor(ContextCompat.getColor(AgentDashboard.this, R.color.buttonAccentText));
                    submitBtn.setBackgroundColor(ContextCompat.getColor(AgentDashboard.this, R.color.buttonAccentBg));
                    activateRegistrationSection();
                },
                position -> showDeleteConfirmation(position));
        recyclerViewServants.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availabilityOptions);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availabilitySpinner.setAdapter(availabilityAdapter);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
    }

    private void setupInputValidation() {
        tvName.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isWhitespace(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        mobile.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(10),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        aadharNumber.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(12),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
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
            public void afterTextChanged(Editable s) {}
        });

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
                    mobileVerifiedIcon.setVisibility(View.VISIBLE);
                } else {
                    mobileVerifiedIcon.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
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

        editUsername.addTextChangedListener(profileTextWatcher);
        editMobile.addTextChangedListener(profileTextWatcher);
    }

    private void checkProfileChanges() {
        String currentUsername = editUsername.getText().toString().trim();
        String currentMobile = editMobile.getText().toString().trim();
        boolean hasChanges =
                !currentUsername.equals(originalUsername) ||
                        !currentMobile.equals(originalMobile) ||
                        !Objects.equals(currentImageUrl, originalAgentImageUrl);
        isProfileChanged = hasChanges;
        btnUpdateProfile.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
    }

    private void loadAgentProfile() {
        agentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    originalUsername = snapshot.child("username").getValue(String.class);
                    originalMobile = snapshot.child("mobile_number").getValue(String.class);
                    String currentEmail = snapshot.child("email").getValue(String.class);
                    originalAgentImageUrl = snapshot.child("profile_photo").getValue(String.class);

                    editUsername.setText(originalUsername);
                    editEmail.setText(currentEmail);
                    editMobile.setText(originalMobile);
                    if (originalAgentImageUrl != null && !originalAgentImageUrl.isEmpty()) {
                        currentImageUrl = originalAgentImageUrl;
                        Glide.with(AgentDashboard.this)
                                .load(originalAgentImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(agentProfilePicture);
                    } else {
                        currentImageUrl = null;
                        agentProfilePicture.setImageResource(R.drawable.default_profile);
                    }
                    checkProfileChanges();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastUtil.error(AgentDashboard.this, "Failed to load profile: " + error.getMessage());
            }
        });
    }

    private void fetchServantsFromFirebase() {
        databaseReference.orderByChild("agentId").equalTo(agentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        servantList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ServantRegistrationModel model = snap.getValue(ServantRegistrationModel.class);
                            if (model != null) {
                                model.setId(snap.getKey());
                                servantList.add(model);
                            }
                        }
                        updateDashboardCounts();
                        filterServants(editFilter.getText().toString(), filterSpinner.getSelectedItem().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ToastUtil.error(AgentDashboard.this, "Failed to fetch servants: " + error.getMessage());
                    }
                });
    }

    private void filterServants(String query, String filterType) {
        filteredServantList.clear();
        query = query.toLowerCase().trim();
        for (ServantRegistrationModel servant : servantList) {
            boolean matchesQuery = servant.getName().toLowerCase().contains(query) ||
                    servant.getMobile().contains(query) ||
                    servant.getCurrentAddress().toLowerCase().contains(query);
            boolean matchesFilter = filterType.equals("All") ||
                    (filterType.equals("Verified") && servant.getIsVerified()) ||
                    (filterType.equals("Unverified") && !servant.getIsVerified());
            if (matchesQuery && matchesFilter) {
                filteredServantList.add(servant);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateDashboardCounts() {
        int total = servantList.size();
        int verified = 0;
        for (ServantRegistrationModel servant : servantList) {
            if (servant.getIsVerified()) {
                verified++;
            }
        }
        int unverified = total - verified;
        tvTotalRegistration.setText(String.valueOf(total));
        tvVerified.setText(String.valueOf(verified));
        tvUnverified.setText(String.valueOf(unverified));
    }

    private void handleSubmit() {
        String name = tvName.getText().toString().trim();
        String mobileStr = mobile.getText().toString().trim();
        String address = currentAddress.getText().toString().trim();
        String areaStr = area.getText().toString().trim();
        String exp = experience.getText().toString().trim();
        String aadhar = aadharNumber.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String availability = availabilitySpinner.getSelectedItem().toString();
        String urgentChargeStr = urgentCharge.getText().toString().trim();
        String expectedSalary = editSalary.getText().toString().trim();
        if (urgentChargeStr.isEmpty() || urgentChargeStr.matches("^0+(\\.0+)?$") || urgentChargeStr.startsWith("0")) {
            urgentChargeStr = "Not Available";
        }

        if (!validateInputs(name, mobileStr, address, areaStr, exp, aadhar, category, gender, availability, expectedSalary)) {
            return;
        }

        String servantId = isEditing ? editingServantId : "";
        String createdAt = isEditing ? filteredServantList.stream()
                .filter(s -> s.getId().equals(editingServantId))
                .findFirst()
                .map(ServantRegistrationModel::getCreatedAt)
                .orElse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        ServantRegistrationModel model = new ServantRegistrationModel(
                name, servantId, mobileStr, address, areaStr, exp, aadhar, category, gender, availability,
                urgentChargeStr, expectedSalary, cbVerifyAadhar.isChecked(), agentId, createdAt, currentImageUrl);

        if (!isEditing) {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(PaymentActivity.EXTRA_AGENT_ID, agentId);
            intent.putExtra(PaymentActivity.EXTRA_SERVANT_MODEL, model);
            startActivityForResult(intent, PAYMENT_REQUEST_CODE);
        } else {
            databaseReference.child(servantId).setValue(model)
                    .addOnSuccessListener(aVoid -> {
                        ToastUtil.success(this, "Servant updated");
                        clearForm();
                        activateHomeSection();
                    })
                    .addOnFailureListener(e -> ToastUtil.error(this, "Failed to save: " + e.getMessage()));
        }
    }

    private boolean validateInputs(String name, String mobileStr, String address, String areaStr, String exp, String aadhar, String category, String gender, String availability, String expectedSalary) {
        if (name.isEmpty()) {
            tvName.setError("Name is required");
            return false;
        }
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            tvName.setError("Only alphabets allowed in name");
            return false;
        }
        if (!mobileStr.matches("^[6-9][0-9]{9}$")) {
            mobile.setError("Valid 10-digit mobile number is required");
            return false;
        }
        if (address.isEmpty()) {
            currentAddress.setError("Address is required");
            return false;
        }
        if (areaStr.isEmpty()) {
            area.setError("Area is required");
            return false;
        }
        if (exp.isEmpty()) {
            experience.setError("Experience is required");
            return false;
        }
        if (!aadhar.matches("^[2-9][0-9]{11}$")) {
            aadharNumber.setError("Valid 12-digit Aadhar number is required");
            return false;
        }
        if (category.equals("Select category")) {
            ToastUtil.info(this, "Please select a category");
            return false;
        }
        if (gender.equals("Gender")) {
            ToastUtil.info(this, "Please select a gender");
            return false;
        }
        if (availability.equals("Availability")) {
            ToastUtil.info(this, "Please select availability");
            return false;
        }
        if (expectedSalary.isEmpty()) {
            editSalary.setError("Expected salary is required");
            return false;
        }
        if (!expectedSalary.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            editSalary.setError("Enter a valid amount.");
            return false;
        }
        return true;
    }

    private void sendPaymentSuccess(String toEmail, String agentName, String servantName, double originalAmount, String redeemCode, double finalAmount, String paymentId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Servant...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(toEmail).matches()) {
            ToastUtil.error(this, "Invalid agent email");
            progressDialog.dismiss();
            return;
        }

        String subject = "Payment Receipt - Servant Registration";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;'>")
                .append("<div style='max-width: 700px; margin: auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>")
                .append("<div style='background-color: #117A8B; padding: 25px 30px; text-align: center;'>")
                .append("<img src='https://i.postimg.cc/SjS36vcT/payment-app-logo.png' alt='HireEasy Logo' style='height: 70px; margin-bottom: 10px;' />")
                .append("<h2 style='color: #ffffff; font-size: 24px; margin: 0;'>Payment Receipt</h2>")
                .append("</div>")
                .append("<div style='padding: 30px;'>")
                .append("<p style='font-size: 16px; color: #333333;'>Hello ").append(agentName).append(",</p>")
                .append("<p style='font-size: 16px; color: #333333; margin-top: 10px;'>Thank you for registering a servant with <strong>HireEasy</strong>. We’re confirming that your payment has been received successfully. Below are the transaction details:</p>")
                .append("<table style='width: 100%; border-collapse: collapse; margin: 30px 0; font-size: 15px;'>")
                .append("<thead>")
                .append("<tr style='background-color: #E9ECEF;'>")
                .append("<th style='padding: 12px; text-align: left; border: 1px solid #dee2e6;'>Field</th>")
                .append("<th style='padding: 12px; text-align: left; border: 1px solid #dee2e6;'>Details</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6;'>Payment ID</td><td style='padding: 12px; border: 1px solid #dee2e6;'>").append(paymentId).append("</td></tr>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6;'>Agent Name</td><td style='padding: 12px; border: 1px solid #dee2e6;'>").append(agentName).append("</td></tr>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6;'>Servant Name</td><td style='padding: 12px; border: 1px solid #dee2e6;'>").append(servantName).append("</td></tr>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6;'>Registration Charge</td><td style='padding: 12px; border: 1px solid #dee2e6;'>Rs. ").append(String.format("%.2f", originalAmount)).append("</td></tr>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6;'>Redeem Code</td><td style='padding: 12px; border: 1px solid #dee2e6;'>").append(redeemCode.isEmpty() ? "N/A" : redeemCode).append("</td></tr>")
                .append("<tr><td style='padding: 12px; border: 1px solid #dee2e6; font-weight: bold;'>Final Amount Paid</td><td style='padding: 12px; border: 1px solid #dee2e6; font-weight: bold;'>Rs. ").append(String.format("%.2f", finalAmount)).append("</td></tr>")
                .append("</tbody>")
                .append("</table>")
                .append("<p style='font-size: 15px; color: #6c757d;'>This receipt confirms that the servant registration has been completed and payment has been successfully processed.</p>")
                .append("<p style='font-size: 15px; color: #6c757d;'>You can now manage your servant profile from your HireEasy dashboard.</p>")
                .append("</div>")
                .append("<div style='background-color: #f1f1f1; padding: 20px 30px; text-align: center;'>")
                .append("<p style='font-size: 13px; color: #6c757d; margin: 0;'>Need assistance? Contact us at <a href='mailto:hireeasy1@gmail.com' style='color: #117A8B; text-decoration: none;'>support@hireeasy.com</a></p>")
                .append("<p style='font-size: 13px; color: #6c757d; margin: 5px 0 0;'>© 2025 HireEasy. All rights reserved.</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>");
        String body = sb.toString();

        new Thread(() -> {
            try {
                GMailSender.send(toEmail, subject, body);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    ToastUtil.error(this, "Failed to send receipt: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleUpdateProfile() {
        String newUsername = editUsername.getText().toString().trim();
        String newMobile = editMobile.getText().toString().trim();

        if (newUsername.isEmpty() || newMobile.isEmpty()) {
            ToastUtil.info(this, "Please fill all fields!");
            return;
        }
        if (!newUsername.matches("^[a-zA-Z\\s]+$")) {
            editUsername.setError("Only alphabets allowed in username");
            return;
        }
        if (!newMobile.matches("^[6-9][0-9]{9}$")) {
            editMobile.setError("Valid 10-digit mobile number is required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("mobile_number", newMobile);
        updates.put("profile_photo", currentImageUrl != null ? currentImageUrl : "");

        agentReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    ToastUtil.success(AgentDashboard.this, "Profile updated successfully");
                    originalUsername = newUsername;
                    originalMobile = newMobile;
                    originalAgentImageUrl = currentImageUrl;
                    isProfileChanged = false;
                    btnUpdateProfile.setVisibility(View.GONE);
                    loadAgentProfile();
                })
                .addOnFailureListener(e -> ToastUtil.error(AgentDashboard.this, "Failed to update profile: " + e.getMessage()));
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_password, null);
        EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);
        ImageView eyeIcon = dialogView.findViewById(R.id.eyeIcon);
        ImageView eyeIconSame = dialogView.findViewById(R.id.eyeIconSame);
        MaterialButton changePasswordBtn = dialogView.findViewById(R.id.changePassword);

        eyeIcon.setOnClickListener(v -> {
            if (oldPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                oldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.eye_slash);
            } else {
                oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.eye);
            }
            oldPassword.setSelection(oldPassword.getText().length());
        });

        eyeIconSame.setOnClickListener(v -> {
            if (newPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconSame.setImageResource(R.drawable.eye_slash);
            } else {
                newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconSame.setImageResource(R.drawable.eye);
            }
            newPassword.setSelection(newPassword.getText().length());
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        changePasswordBtn.setOnClickListener(v -> {
            applyButtonAnimation(v);
            String oldPass = oldPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                ToastUtil.info(this, "Please fill all fields!");
                return;
            }

            if (newPass.length() < 6) {
                newPassword.setError("New password must be at least 6 characters long");
                return;
            }

            String hashedOldPassword = hashPassword(oldPass);

            agentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbPassword = snapshot.child("password").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(hashedOldPassword)) {
                            String hashedNewPassword = hashPassword(newPass);
                            agentReference.child("password").setValue(hashedNewPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        ToastUtil.success(AgentDashboard.this, "Password updated successfully");
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> ToastUtil.error(AgentDashboard.this, "Failed to update password: " + e.getMessage()));
                        } else {
                            oldPassword.setError("Incorrect current password");
                        }
                    } else {
                        ToastUtil.error(AgentDashboard.this, "Agent data not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastUtil.error(AgentDashboard.this, "Database error: " + error.getMessage());
                }
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmation(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this servant?")
                .setIcon(R.drawable.delete_profile)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String servantId = filteredServantList.get(position).getId();
                    databaseReference.child(servantId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                ToastUtil.success(AgentDashboard.this, "Servant deleted!");
                                clearForm();
                            })
                            .addOnFailureListener(e -> ToastUtil.error(AgentDashboard.this, "Failed to delete: " + e.getMessage()));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorDanger));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
    }

    private void showLogoutConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.logout)
                .setPositiveButton("Yes", (d, which) -> {
                    ToastUtil.success(this, "Logout Successfully!");
                    new SessionManager(this).logout(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
        });
        dialog.show();
    }

    private void activateHomeSection() {
        homeSection.setVisibility(View.VISIBLE);
        registrationSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.GONE);
        applyFadeAnimation(homeSection);
        homeSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_bg_agent));
        homeSectionButton.setImageResource(R.drawable.home_icon_active);
        registerSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        registerSectionButton.setImageResource(R.drawable.register_icon_user);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profile_icon_user);
        clearForm();
    }

    private void activateRegistrationSection() {
        homeSection.setVisibility(View.GONE);
        registrationSection.setVisibility(View.VISIBLE);
        profileSection.setVisibility(View.GONE);
        applyFadeAnimation(registrationSection);
        registerSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_bg_agent));
        registerSectionButton.setImageResource(R.drawable.register_icon_active);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon_agent);
        profileSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        profileSectionButton.setImageResource(R.drawable.profile_icon_user);
    }

    private void activateProfileSection() {
        homeSection.setVisibility(View.GONE);
        registrationSection.setVisibility(View.GONE);
        profileSection.setVisibility(View.VISIBLE);
        applyFadeAnimation(profileSection);
        profileSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_bg_agent));
        profileSectionButton.setImageResource(R.drawable.profile_icon_active);
        homeSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        homeSectionButton.setImageResource(R.drawable.home_icon_agent);
        registerSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        registerSectionButton.setImageResource(R.drawable.register_icon_user);
        clearForm();
        loadAgentProfile();
    }

    private void clearForm() {
        register_servant.setText("Register Servants");
        tvName.setText("");
        tvName.setError(null);
        mobile.setText("");
        mobile.setError(null);
        currentAddress.setText("");
        currentAddress.setError(null);
        area.setError(null);
        area.setText("");
        experience.setText("");
        experience.setError(null);
        aadharNumber.setText("");
        aadharNumber.setError(null);
        categorySpinner.setSelection(0);
        genderSpinner.setSelection(0);
        availabilitySpinner.setSelection(0);
        urgentCharge.setText("");
        urgentCharge.setError(null);
        editSalary.setText("");
        editSalary.setError(null);
        cbVerifyAadhar.setChecked(false);
        mobileVerifiedIcon.setVisibility(View.GONE);
        profilePicture.setImageResource(R.drawable.default_profile);
        submitBtn.setText("Register Servant");
        submitBtn.setTextColor(ContextCompat.getColor(this, R.color.buttonPrimaryText));
        submitBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonPrimaryBg));
        isEditing = false;
        editingServantId = null;
        currentImageUrl = null;
    }

    private void populateFormForEdit(ServantRegistrationModel model) {
        tvName.setText(model.getName());
        mobile.setText(model.getMobile());
        currentAddress.setText(model.getCurrentAddress());
        area.setText(model.getArea());
        experience.setText(model.getExperience());
        aadharNumber.setText(model.getAadharNumber());
        urgentCharge.setText(model.getUrgentCharge());
        editSalary.setText(model.getExpectedSalary());
        cbVerifyAadhar.setChecked(model.getIsVerified());
        mobileVerifiedIcon.setVisibility(model.getMobile().matches("^[6-9][0-9]{9}$") ? View.VISIBLE : View.GONE);
        currentImageUrl = model.getProfilePhoto();

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.default_profile);
        }

        for (int i = 0; i < categoryOptions.length; i++) {
            if (categoryOptions[i].equals(model.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equals(model.getGender())) {
                genderSpinner.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < availabilityOptions.length; i++) {
            if (availabilityOptions[i].equals(model.getAvailability())) {
                availabilitySpinner.setSelection(i);
                break;
            }
        }
    }

    private String generateServantId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("S");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void checkAndRequestPermissions(boolean isServantImage) {
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
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), isServantImage ? PERMISSION_REQUEST_CODE : PERMISSION_REQUEST_CODE + 1);
        } else {
            showImageOptions(isServantImage);
        }
    }

    private void showImageOptions(boolean isServantImage) {
        String[] options = {"Upload from Camera", "Upload from Gallery", "Delete Image"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera(isServantImage);
                    } else if (which == 1) {
                        openGallery(isServantImage);
                    } else {
                        currentImageUrl = "";
                        ImageView targetImageView = isServantImage ? profilePicture : agentProfilePicture;
                        targetImageView.setImageResource(R.drawable.default_profile);
                        if (isServantImage && isEditing) {
                            databaseReference.child(editingServantId).child("profilePhoto").setValue("")
                                    .addOnSuccessListener(aVoid -> ToastUtil.success(AgentDashboard.this, "Servant image deleted"))
                                    .addOnFailureListener(e -> ToastUtil.error(AgentDashboard.this, "Failed to delete servant image: " + e.getMessage()));
                        } else if (!isServantImage) {
                            agentReference.child("profile_photo").setValue("")
                                    .addOnSuccessListener(aVoid -> {
                                        ToastUtil.success(AgentDashboard.this, "Agent image deleted");
                                        originalAgentImageUrl = "";
                                        checkProfileChanges();
                                    })
                                    .addOnFailureListener(e -> ToastUtil.error(AgentDashboard.this, "Failed to delete agent image: " + e.getMessage()));
                        }
                    }
                })
                .show();
    }

    private void openCamera(boolean isServantImage) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, isServantImage ? CAMERA_REQUEST_CODE : CAMERA_REQUEST_CODE + 1);
        } else {
            ToastUtil.error(this, "Camera app not found");
        }
    }

    private void openGallery(boolean isServantImage) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, isServantImage ? GALLERY_REQUEST_CODE : GALLERY_REQUEST_CODE + 1);
        } else {
            ToastUtil.error(this, "Gallery app not found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            handlePaymentResult(resultCode, data);
        } else if (requestCode == CAMERA_REQUEST_CODE || requestCode == GALLERY_REQUEST_CODE) {
            handleServantImageResult(requestCode, resultCode, data);
        } else if (requestCode == CAMERA_REQUEST_CODE + 1 || requestCode == GALLERY_REQUEST_CODE + 1) {
            handleAgentImageResult(requestCode, resultCode, data);
        }
    }

    private void handlePaymentResult(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            ServantRegistrationModel servantModel = data.getParcelableExtra(PaymentActivity.EXTRA_SERVANT_MODEL);
            double paymentAmount = data.getDoubleExtra(PaymentActivity.EXTRA_PAYMENT_AMOUNT, 0.0);
            String redeemCode = data.getStringExtra(PaymentActivity.EXTRA_REDEEM_CODE);
            String agentEmail = data.getStringExtra(PaymentActivity.EXTRA_AGENT_EMAIL);
            String paymentId = data.getStringExtra(PaymentActivity.EXTRA_PAYMENT_ID);
            sendPaymentSuccess(agentEmail, username, servantModel.getName(), 50.0, redeemCode, paymentAmount, paymentId);
            ToastUtil.success(this, "Servant registered successfully");
            clearForm();
            activateHomeSection();
        } else if (resultCode == RESULT_CANCELED) {
            ToastUtil.error(this, "Payment Failed");
        }
    }

    private void handleServantImageResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            ToastUtil.error(this, "Image selection cancelled");
            return;
        }

        try {
            Bitmap bitmap = null;
            if (requestCode == CAMERA_REQUEST_CODE) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    profilePicture.setImageBitmap(bitmap);
                    uploadImageToImgBB(bitmap, false);
                } else {
                    ToastUtil.error(this, "Failed to capture image from camera");
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    // Load image into ImageView using Glide for better performance
                    Glide.with(this)
                            .load(selectedImage)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(profilePicture);
                    // Convert Uri to Bitmap for upload
                    bitmap = getBitmapFromUri(selectedImage);
                    if (bitmap != null) {
                        uploadImageToImgBB(bitmap, false);
                    } else {
                        ToastUtil.error(this, "Failed to load image from gallery");
                    }
                } else {
                    ToastUtil.error(this, "No image selected from gallery");
                }
            }
        } catch (Exception e) {
            ToastUtil.error(this, "Error processing image: " + e.getMessage());
        }
    }

    private void handleAgentImageResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            ToastUtil.error(this, "Image selection cancelled");
            return;
        }

        try {
            Bitmap bitmap = null;
            if (requestCode == CAMERA_REQUEST_CODE + 1) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    agentProfilePicture.setImageBitmap(bitmap);
                    uploadImageToImgBB(bitmap, true);
                } else {
                    ToastUtil.error(this, "Failed to capture image from camera");
                }
            } else if (requestCode == GALLERY_REQUEST_CODE + 1) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    // Load image into ImageView using Glide
                    Glide.with(this)
                            .load(selectedImage)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(agentProfilePicture);
                    // Convert Uri to Bitmap for upload
                    bitmap = getBitmapFromUri(selectedImage);
                    if (bitmap != null) {
                        uploadImageToImgBB(bitmap, true);
                    } else {
                        ToastUtil.error(this, "Failed to load image from gallery");
                    }
                } else {
                    ToastUtil.error(this, "No image selected from gallery");
                }
            }
        } catch (Exception e) {
            ToastUtil.error(this, "Error processing image: " + e.getMessage());
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

    private void uploadImageToImgBB(Bitmap bitmap, boolean isAgentImage) {
        imageUploadDialog.show();
        btnUpdateProfile.setEnabled(false);
        submitBtn.setEnabled(false);

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
                    btnUpdateProfile.setEnabled(true);
                    submitBtn.setEnabled(true);
                    ToastUtil.error(AgentDashboard.this, "Image upload failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    imageUploadDialog.dismiss();
                    btnUpdateProfile.setEnabled(true);
                    submitBtn.setEnabled(true);
                });
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            String newImageUrl = json.getJSONObject("data").getString("url");
                            runOnUiThread(() -> {
                                currentImageUrl = newImageUrl;
                                ImageView targetImageView = isAgentImage ? agentProfilePicture : profilePicture;
                                Glide.with(AgentDashboard.this)
                                        .load(newImageUrl)
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .into(targetImageView);
                                ToastUtil.success(AgentDashboard.this, "Image uploaded successfully");
                                if (isAgentImage) {
                                    checkProfileChanges();
                                }
                            });
                        } else {
                            runOnUiThread(() -> ToastUtil.error(AgentDashboard.this, "Image upload failed"));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> ToastUtil.error(AgentDashboard.this, "Error parsing response: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> ToastUtil.error(AgentDashboard.this, "Image upload failed: " + response.message()));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE || requestCode == PERMISSION_REQUEST_CODE + 1) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                showImageOptions(requestCode == PERMISSION_REQUEST_CODE);
            } else {
                ToastUtil.error(this, "Required permissions denied");
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private void applyButtonAnimation(View view) {
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        view.startAnimation(scale);
    }

    private void applyFadeAnimation(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(fadeIn);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (homeSection.getVisibility() == View.VISIBLE) {
            // Close the app completely
            finishAffinity(); // This will close all activities and exit the app
        } else {
            activateHomeSection(); // Navigate back to home section
        }
    }
}