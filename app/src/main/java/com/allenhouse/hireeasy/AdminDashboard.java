package com.allenhouse.hireeasy;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdminDashboard extends BaseActivity {

    private EditText editUsername, editEmail, editMobile, editPassword, editFilter;
    private EditText editCode, editAmount, editUsageTime, editExpireDate;
    private ImageView eyeIcon;
    private Button btnSubmit, btnCreate;
    private ImageButton registerSectionButton, listSectionButton, redeemSectionButton, logoutButton;
    private RecyclerView recyclerView, recyclerViewRedeem;
    private TextView textTotalAgents, register_head, title_text;
    private LinearLayout registrationSection, agentListSection, redeemCodeSection, topAgentsContainer, registerSectionSwitch, listSectionSwitch, redeemSectionSwitch, logoutButtonSwitch;
    private List<AgentRegistrationModel> agentList, filteredAgentList;
    private List<RedeemCodeModel> redeemCodeList;
    private AgentRegistrationAdapter adapter;
    private RedeemCodeAdapter redeemCodeAdapter;
    private int editPosition = -1;
    private boolean isEditing = false;
    private boolean isPasswordVisible = false;
    private boolean isRegistrationSectionVisible = true;
    private String adminId;
    private DatabaseReference databaseReference, redeemDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // Get admin_id from Intent
        adminId = getIntent().getStringExtra("admin_id");
        if (adminId == null) {
            ToastUtil.error(AdminDashboard.this, "Admin ID not found!");
            finish();
            return;
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("agents");
        redeemDatabaseReference = FirebaseDatabase.getInstance().getReference().child("redeem_codes");

        // Initialize views
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editMobile = findViewById(R.id.editMobile);
        editPassword = findViewById(R.id.editPassword);
        eyeIcon = findViewById(R.id.eyeIcon);
        btnSubmit = findViewById(R.id.btnSubmit);
        registerSectionButton = findViewById(R.id.registerSectionButton);
        listSectionButton = findViewById(R.id.listSectionButton);
        registerSectionSwitch = findViewById(R.id.registerSectionSwitch);
        listSectionSwitch = findViewById(R.id.listSectionSwitch);
        logoutButton = findViewById(R.id.logoutButton);
        logoutButtonSwitch = findViewById(R.id.logoutButtonSwitch);
        recyclerView = findViewById(R.id.recyclerView);
        textTotalAgents = findViewById(R.id.textTotalAgents);
        registrationSection = findViewById(R.id.registrationSection);
        agentListSection = findViewById(R.id.agentListSection);
        editFilter = findViewById(R.id.editFilter);
        topAgentsContainer = findViewById(R.id.topAgentsContainer);
        register_head = findViewById(R.id.register_head);
        title_text = findViewById(R.id.title_text);
        redeemCodeSection = findViewById(R.id.redeemCodeSection);
        redeemSectionButton = findViewById(R.id.redeemSectionButton);
        redeemSectionSwitch = findViewById(R.id.redeemSectionSwitch);
        editCode = findViewById(R.id.editCode);
        editAmount = findViewById(R.id.editAmount);
        editUsageTime = findViewById(R.id.editUsageTime);
        editExpireDate = findViewById(R.id.editExpireDate);
        btnCreate = findViewById(R.id.btnCreate);
        recyclerViewRedeem = findViewById(R.id.recyclerViewRedeem);

        // Initialize agent lists
        agentList = new ArrayList<>();
        filteredAgentList = new ArrayList<>();
        adapter = new AgentRegistrationAdapter(filteredAgentList,
                (model, position) -> {
                    // Edit Mode Setup
                    title_text.setText("Update Agent");
                    register_head.setText("Update Agent Data");
                    editUsername.setText(model.getName());
                    editEmail.setText(model.getEmail());
                    editMobile.setText(model.getMobile());
                    editPassword.setText("");
                    isEditing = true;
                    editPosition = position;
                    btnSubmit.setText("Update");
                    btnSubmit.setTextColor(ContextCompat.getColorStateList(this, R.color.buttonAccentText));
                    btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.buttonAccentBg));
                    switchToRegistrationSection();
                },
                position -> showDeleteConfirmation(position)
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize redeem code list
        redeemCodeList = new ArrayList<>();
        redeemCodeAdapter = new RedeemCodeAdapter(redeemCodeList, position -> showDeleteRedeemConfirmation(position));
        recyclerViewRedeem.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRedeem.setAdapter(redeemCodeAdapter);

        // Fetch agents from Firebase
        fetchAgentsFromFirebase();

        // Fetch redeem codes from Firebase
        fetchRedeemCodesFromFirebase();

        // Setup input validation
        setupInputValidation();

        // Toggle password visibility
        eyeIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.eye);
            } else {
                editPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.eye_slash);
            }
            editPassword.setSelection(editPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Set input filter for username to allow only alphabets and spaces
        editUsername.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        char c = source.charAt(i);
                        if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Set input filter for mobile to allow only digits
        editMobile.setFilters(new InputFilter[]{
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

        // Convert Redeem Code to uppercase
        editCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                String upperCase = input.toUpperCase();
                if (!input.equals(upperCase)) {
                    editCode.setText(upperCase);
                    editCode.setSelection(upperCase.length());
                }
            }
        });

        // Date picker for expire date
        editExpireDate.setOnClickListener(v -> showDatePickerDialog());

        // Handle submit/update button
        btnSubmit.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || (password.isEmpty() && !isEditing)) {
                ToastUtil.info(AdminDashboard.this, "Please fill all required fields!");
                return;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Please enter a valid email address");
                return;
            } else if (mobile.length() != 10 || !mobile.matches("^[5-9][0-9]{9}$")) {
                editMobile.setError("Mobile number must be 10 digits starting from 5-9");
                return;
            } else if (!username.matches("^[a-zA-Z\\s]+$")) {
                editUsername.setError("Only alphabets allowed in username");
                return;
            }

            String hashedPassword = password.isEmpty() ? null : hashPassword(password);
            String agentId = isEditing ? filteredAgentList.get(editPosition).getAgentId() : generateAgentId();
            String createdAt = isEditing ? filteredAgentList.get(editPosition).getCreatedAt()
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            if (!isEditing) {
                // Check if email already exists in Firebase
                databaseReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    ToastUtil.error(AdminDashboard.this, "Agent with this email already exists");
                                } else {
                                    saveAgentToFirebase(agentId, username, email, mobile, hashedPassword, createdAt);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                ToastUtil.error(AdminDashboard.this, "Error checking email: " + error.getMessage());
                            }
                        });
            } else {
                saveAgentToFirebase(agentId, username, email, mobile, hashedPassword, createdAt);
            }
        });

        // Handle create redeem code button
        btnCreate.setOnClickListener(v -> {
            String code = editCode.getText().toString().trim();
            String amount = editAmount.getText().toString().trim();
            String usageTime = editUsageTime.getText().toString().trim();
            String expireDate = editExpireDate.getText().toString().trim();

            if (code.isEmpty() || amount.isEmpty()) {
                ToastUtil.info(AdminDashboard.this, "Please fill Code and Amount fields!");
                return;
            }

            String finalUsageTime = usageTime.isEmpty() ? "Unlimited" : usageTime;
            String finalExpireDate = expireDate.isEmpty() ? "No Expiry Date" : expireDate;
            String redeemId = generateRedeemId();
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            saveRedeemCodeToFirebase(redeemId, code, amount, finalUsageTime, finalExpireDate, createdAt);
        });

        // Handle section switching
        registerSectionSwitch.setOnClickListener(v -> activateRegisterSection());
        registerSectionButton.setOnClickListener(v -> activateRegisterSection());

        listSectionSwitch.setOnClickListener(v -> activateListSection());
        listSectionButton.setOnClickListener(v -> activateListSection());

        redeemSectionSwitch.setOnClickListener(v -> activateRedeemSection());
        redeemSectionButton.setOnClickListener(v -> activateRedeemSection());

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        logoutButtonSwitch.setOnClickListener(v -> showLogoutConfirmation());

        // Add TextWatcher for filter input
        editFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAgents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Update total agents
        updateTotalAgents();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    editExpireDate.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.show();
        Button positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonPrimaryBg));
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.buttonPrimaryText));

        negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonSecondaryBg));
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));

    }

    private void saveAgentToFirebase(String agentId, String username, String email, String mobile,
                                     String hashedPassword, String createdAt) {
        if (isEditing) {
            // Partial update for existing agent
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", username);
            updates.put("email", email);
            updates.put("mobile_number", mobile);
            if (hashedPassword != null) {
                updates.put("password", hashedPassword);
            }
            databaseReference.child(agentId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        ToastUtil.success(AdminDashboard.this, "Agent updated");
                        switchToAgentListSection();
                        resetEditState();
                    })
                    .addOnFailureListener(e -> {
                        ToastUtil.error(AdminDashboard.this, "Failed to update: " + e.getMessage());
                    });
        } else {
            // Full write for new agent
            Map<String, Object> agentData = new HashMap<>();
            agentData.put("agent_id", agentId);
            agentData.put("admin_id", adminId);
            agentData.put("username", username);
            agentData.put("profile_photo", String.valueOf(R.drawable.default_profile));
            agentData.put("email", email);
            agentData.put("mobile_number", mobile);
            agentData.put("password", hashedPassword);
            agentData.put("created_at", createdAt);

            databaseReference.child(agentId).setValue(agentData)
                    .addOnSuccessListener(aVoid -> {
                        ToastUtil.success(AdminDashboard.this, "Agent registered");
                        resetEditState();
                    })
                    .addOnFailureListener(e -> {
                        ToastUtil.error(AdminDashboard.this, "Failed to save: " + e.getMessage());
                    });
        }
    }

    private void saveRedeemCodeToFirebase(String redeemId, String code, String amount, String usageTime,
                                          String expireDate, String createdAt) {
        Map<String, Object> redeemData = new HashMap<>();
        redeemData.put("redeem_id", redeemId);
        redeemData.put("code", code);
        redeemData.put("amount", amount);
        redeemData.put("usage_time", usageTime);
        redeemData.put("expire_date", expireDate);
        redeemData.put("created_at", createdAt);

        redeemDatabaseReference.child(redeemId).setValue(redeemData)
                .addOnSuccessListener(aVoid -> {
                    ToastUtil.success(AdminDashboard.this, "Redeem code created");
                    editCode.setText("");
                    editAmount.setText("");
                    editUsageTime.setText("");
                    editExpireDate.setText("");
                })
                .addOnFailureListener(e -> {
                    ToastUtil.error(AdminDashboard.this, "Failed to save redeem code: " + e.getMessage());
                });
    }

    private void showLogoutConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(AdminDashboard.this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.logout)
                .setPositiveButton("Yes", (d, which) -> {
                    ToastUtil.success(AdminDashboard.this, "Logout Successfully!");
                    new SessionManager(AdminDashboard.this).logout(AdminDashboard.this);
                    Intent intent = new Intent(AdminDashboard.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            dialog.setCanceledOnTouchOutside(false);
        });
        dialog.show();
    }

    private void showDeleteConfirmation(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this agent?")
                .setIcon(R.drawable.delete_profile)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String agentId = filteredAgentList.get(position).getAgentId();
                    databaseReference.child(agentId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                ToastUtil.success(AdminDashboard.this, "Agent Deleted!");
                                resetEditState();
                            })
                            .addOnFailureListener(e -> {
                                ToastUtil.error(AdminDashboard.this, "Failed to delete: " + e.getMessage());
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorDanger));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
    }

    private void showDeleteRedeemConfirmation(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this redeem code?")
                .setIcon(R.drawable.delete_profile)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String redeemId = redeemCodeList.get(position).getRedeemId();
                    redeemDatabaseReference.child(redeemId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                ToastUtil.success(AdminDashboard.this, "Redeem Code Deleted!");
                            })
                            .addOnFailureListener(e -> {
                                ToastUtil.error(AdminDashboard.this, "Failed to delete: " + e.getMessage());
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorDanger));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
    }

    private void activateRegisterSection() {
        switchToRegistrationSection();
        resetEditState();

        registerSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        registerSectionButton.setImageResource(R.drawable.register_icon_active);

        listSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        listSectionButton.setImageResource(R.drawable.list_icon);

        redeemSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        redeemSectionButton.setImageResource(R.drawable.redeem_code);
    }

    private void activateListSection() {
        switchToAgentListSection();
        resetEditState();

        listSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        listSectionButton.setImageResource(R.drawable.list_icon_active);

        registerSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        registerSectionButton.setImageResource(R.drawable.register_icon);

        redeemSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        redeemSectionButton.setImageResource(R.drawable.redeem_code);
    }

    private void activateRedeemSection() {
        switchToRedeemSection();
        resetEditState();

        redeemSectionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.active_button_background));
        redeemSectionButton.setImageResource(R.drawable.redeem_code_active);

        registerSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        registerSectionButton.setImageResource(R.drawable.register_icon);

        listSectionButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        listSectionButton.setImageResource(R.drawable.list_icon);
    }

    private void resetEditState() {
        title_text.setText("Welcome Admin!");
        register_head.setText("Register New Agent");
        editUsername.setText("");
        editUsername.setError(null);
        editEmail.setText("");
        editEmail.setError(null);
        editMobile.setText("");
        editMobile.setError(null);
        editPassword.setText("");
        isEditing = false;
        editPosition = -1;
        btnSubmit.setText("Submit");
        btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.buttonPrimaryText));
        btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.buttonPrimaryBg));
    }

    private void switchToRegistrationSection() {
        registrationSection.setVisibility(View.VISIBLE);
        agentListSection.setVisibility(View.GONE);
        redeemCodeSection.setVisibility(View.GONE);
        isRegistrationSectionVisible = true;
    }

    private void switchToAgentListSection() {
        registrationSection.setVisibility(View.GONE);
        agentListSection.setVisibility(View.VISIBLE);
        redeemCodeSection.setVisibility(View.GONE);
        isRegistrationSectionVisible = false;
        resetEditState();
    }

    private void switchToRedeemSection() {
        registrationSection.setVisibility(View.GONE);
        agentListSection.setVisibility(View.GONE);
        redeemCodeSection.setVisibility(View.VISIBLE);
        isRegistrationSectionVisible = false;
        resetEditState();
    }

    private String generateAgentId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("A");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRedeemId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("R");
        for (int i = 0; i < 3; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void setupInputValidation() {
        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty() && !input.matches("^[a-zA-Z\\s]*$")) {
                    editUsername.setError("Only alphabets and spaces allowed");
                } else {
                    editUsername.setError(null);
                    editEmail.setEnabled(!input.isEmpty());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (!input.isEmpty()) {
                    char firstChar = input.charAt(0);
                    if (firstChar < '5' || firstChar > '9') {
                        editMobile.setError("Invalid mobile number.");
                    } else {
                        editMobile.setError(null);
                    }
                } else {
                    editMobile.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchAgentsFromFirebase() {
        DatabaseReference servantsReference = FirebaseDatabase.getInstance().getReference().child("servants");
        Map<String, Integer> agentServantCount = new HashMap<>();

        // First, count verified servants per agentId
        servantsReference.orderByChild("isVerified").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String agentId = snapshot.child("agentId").getValue(String.class);
                            if (agentId != null) {
                                agentServantCount.put(agentId, agentServantCount.getOrDefault(agentId, 0) + 1);
                            }
                        }

                        // Fetch agents and sort by created_at for the full list
                        databaseReference.orderByChild("admin_id").equalTo(adminId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        agentList.clear();
                                        List<AgentRegistrationModel> tempAgentList = new ArrayList<>(); // Temporary list for sorting by created_at
                                        topAgentsContainer.removeAllViews();
                                        LayoutInflater inflater = LayoutInflater.from(AdminDashboard.this);

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String agentId = snapshot.child("agent_id").getValue(String.class);
                                            String username = snapshot.child("username").getValue(String.class);
                                            String email = snapshot.child("email").getValue(String.class);
                                            String mobile = snapshot.child("mobile_number").getValue(String.class);
                                            String password = snapshot.child("password").getValue(String.class);
                                            String profilePhoto = snapshot.child("profile_photo").getValue(String.class);
                                            String createdAt = snapshot.child("created_at").getValue(String.class);
                                            if (agentId != null && username != null && email != null && mobile != null && password != null) {
                                                AgentRegistrationModel agent = new AgentRegistrationModel(
                                                        username, agentId, email, mobile, password, profilePhoto, createdAt);
                                                tempAgentList.add(agent);
                                            }
                                        }

                                        // Sort tempAgentList by created_at (descending)
                                        Collections.sort(tempAgentList, (a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()));
                                        agentList.addAll(tempAgentList); // Assign to agentList for RecyclerView

                                        // Sort top 3 agents by number of verified servants
                                        List<AgentRegistrationModel> topAgents = new ArrayList<>(agentList);
                                        Collections.sort(topAgents, (a1, a2) -> {
                                            int count1 = agentServantCount.getOrDefault(a1.getAgentId(), 0);
                                            int count2 = agentServantCount.getOrDefault(a2.getAgentId(), 0);
                                            return Integer.compare(count2, count1); // Descending order
                                        });

                                        // Display top 3 agents
                                        for (int i = 0; i < Math.min(3, topAgents.size()); i++) {
                                            AgentRegistrationModel agent = topAgents.get(i);
                                            View topAgentView = inflater.inflate(R.layout.top_agent_list, topAgentsContainer, false);
                                            ImageView avatar = topAgentView.findViewById(R.id.avatar);
                                            TextView name = topAgentView.findViewById(R.id.name);

                                            name.setText(agent.getAgentId() + " (" + agentServantCount.getOrDefault(agent.getAgentId(), 0) + " servants)");

                                            if (agent.getProfilePhoto() != null && !agent.getProfilePhoto().isEmpty()) {
                                                Glide.with(AdminDashboard.this)
                                                        .load(agent.getProfilePhoto())
                                                        .placeholder(R.drawable.default_profile)
                                                        .error(R.drawable.default_profile)
                                                        .into(avatar);
                                            } else {
                                                avatar.setImageResource(R.drawable.default_profile);
                                            }

                                            topAgentsContainer.addView(topAgentView);
                                        }

                                        filterAgents(editFilter.getText().toString());
                                        updateTotalAgents();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        ToastUtil.error(AdminDashboard.this, "Failed to fetch agents: " + databaseError.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ToastUtil.error(AdminDashboard.this, "Failed to fetch servants: " + databaseError.getMessage());
                    }
                });
    }

    private void fetchRedeemCodesFromFirebase() {
        redeemDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                redeemCodeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String redeemId = snapshot.child("redeem_id").getValue(String.class);
                    String code = snapshot.child("code").getValue(String.class);
                    String amount = snapshot.child("amount").getValue(String.class);
                    String usageTime = snapshot.child("usage_time").getValue(String.class);
                    String expireDate = snapshot.child("expire_date").getValue(String.class);
                    String createdAt = snapshot.child("created_at").getValue(String.class);
                    if (redeemId != null && code != null && amount != null && usageTime != null && expireDate != null && createdAt != null) {
                        RedeemCodeModel redeemCode = new RedeemCodeModel(redeemId, code, amount, usageTime, expireDate, createdAt);
                        redeemCodeList.add(redeemCode);
                    }
                }
                redeemCodeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ToastUtil.error(AdminDashboard.this, "Failed to fetch redeem codes: " + databaseError.getMessage());
            }
        });
    }

    private void filterAgents(String query) {
        filteredAgentList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredAgentList.addAll(agentList);
        } else {
            query = query.toLowerCase();
            for (AgentRegistrationModel agent : agentList) {
                if (agent.getName().toLowerCase().contains(query) ||
                        agent.getEmail().toLowerCase().contains(query) ||
                        agent.getMobile().contains(query)) {
                    filteredAgentList.add(agent);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateTotalAgents();
    }

    private void updateTotalAgents() {
        textTotalAgents.setText(String.valueOf(agentList.size())); // Use agentList.size() instead of filteredAgentList.size()
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
}