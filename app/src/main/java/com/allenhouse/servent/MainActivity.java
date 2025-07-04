package com.allenhouse.servent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private ImageButton btnEditImage, settingButton;
    private EditText name, mobile, currentAddress, area, experience, aadharNumber;
    private Spinner categorySpinner, genderSpinner, availabilitySpinner;
    private Button submitBtn;
    private CheckBox cbVerifyAadhar;
    private ImageView mobileVerifiedIcon;
    private LinearLayout aadharVerifiedLayout;
    private ImageView aadharVerifiedIcon;
    private TextView aadharVerifiedText;
    private RecyclerView recyclerViewServants;
    private List<ServantModel> servantList;
    private ServantAdapter servantAdapter;
    private EditText searchInput;
    private ImageButton btnAddServant;
    private String editingServantId;
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    private final String[] categoryOptions = {"Select category", "Cook", "Maid", "Driver", "Baby Sitter"};
    private final String[] genderOptions = {"Gender", "Male", "Female", "Other"};
    private final String[] availabilityOptions = {"Availability", "Yes", "No"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "setContentView done");
        initializeViews();
        Log.d(TAG, "initializeViews done");
        setupSpinners();
        setupListeners();

        settingButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, settingButton);
            popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    Intent intent = new Intent(MainActivity.this, activity_profile.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_logout) {
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }


    private void initializeViews() {
        try {
            profilePicture = findViewById(R.id.profile_picture);
            btnEditImage = findViewById(R.id.btn_edit_image);
            name = findViewById(R.id.tvName);
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
            aadharVerifiedLayout = findViewById(R.id.aadhar_verified_layout);
            aadharVerifiedIcon = findViewById(R.id.aadhar_verified_icon);
            aadharVerifiedText = findViewById(R.id.aadhar_verified_text);
            cbVerifyAadhar = findViewById(R.id.cb_verify_aadhar);
            searchInput = findViewById(R.id.search_input);
           // btnAddServant = findViewById(R.id.btn_add_servant);
             settingButton = findViewById(R.id.setting_Button);

            recyclerViewServants = findViewById(R.id.recyclerViewServants);
            recyclerViewServants.setLayoutManager(new LinearLayoutManager(this));

            servantList = new ArrayList<>();
            Log.d(TAG, "servantList initialized");

            servantAdapter = new ServantAdapter(servantList, new ServantAdapter.OnItemActionListener() {
                @Override
                public void onItemClick(int position) {
                    if (position >= 0 && position < servantList.size()) {
                        Toast.makeText(MainActivity.this, "Clicked: " + servantList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onEditClick(ServantModel servant) {
                    editingServantId = servant.getId();
                    Log.d(TAG, "onEditClick: editingServantId set to " + editingServantId);
                    populateFormForEdit(servant);
                    submitBtn.setText("Update Servant");
                }

                @Override
                public void onDeleteClick(ServantModel servant) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Servant")
                            .setMessage("Are you sure you want to delete " + servant.getName() + "?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Log.d(TAG, "onDeleteClick: Deleting servant with ID " + servant.getId());
                                FirebaseDatabase.getInstance()
                                        .getReference("servants")
                                        .child(servant.getId())
                                        .removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Servant deleted", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Failed to delete servant", Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, "Failed to delete servant: " + task.getException());
                                            }
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
            recyclerViewServants.setAdapter(servantAdapter);
            Log.d(TAG, "servantAdapter set to recyclerViewServants");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("servants");
            ref.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    servantList.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ServantModel model = snap.getValue(ServantModel.class);
                        if (model != null) {
                            model.setId(snap.getKey());
                            servantList.add(model);
                            Log.d(TAG, "Servant added: " + model.getName() + ", ID: " + model.getId());
                        } else {
                            Log.e(TAG, "Null model at key: " + snap.getKey());
                        }
                    }
                    Log.d(TAG, "Firebase data loaded, servantList size: " + servantList.size());
                    servantAdapter.notifyDataSetChanged();
                    updateDashboardCounts();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase load failed: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to load servants", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, availabilityOptions);
        availabilityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        availabilitySpinner.setAdapter(availabilityAdapter);
        Log.d(TAG, "Spinners set up");
    }

    @SuppressLint("SetTextI18n")
    private void setupListeners() {
        if (mobile != null) {
            mobile.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String number = s.toString();
                    if (mobileVerifiedIcon != null) {
                        mobileVerifiedIcon.setVisibility(number.matches("^[6-9][0-9]{9}$") ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }

        if (aadharNumber != null) {
            aadharNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String aadhaar = s.toString();
                    if (aadharVerifiedLayout != null && cbVerifyAadhar != null) {
                        aadharVerifiedLayout.setVisibility(aadhaar.matches("^[2-9][0-9]{11}$") && cbVerifyAadhar.isChecked() ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }

        if (cbVerifyAadhar != null) {
            cbVerifyAadhar.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String aadhaar = aadharNumber != null ? aadharNumber.getText().toString() : "";
                if (aadharVerifiedLayout != null) {
                    aadharVerifiedLayout.setVisibility(isChecked && aadhaar.matches("^[2-9][0-9]{11}$") ? View.VISIBLE : View.GONE);
                }
            });
        }

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    filterServants(s.toString());
                }
            });
        }

        if (btnAddServant != null) {
            btnAddServant.setOnClickListener(v -> {
                clearForm();
                submitBtn.setText("Register Servant");
                editingServantId = null;
                Log.d(TAG, "btnAddServant clicked, form cleared");
            });
        }

        if (btnEditImage != null) {
            btnEditImage.setOnClickListener(v -> checkAndRequestPermissions());
        }

        if (submitBtn != null) {
            submitBtn.setOnClickListener(v -> {
                if (validateInputs()) {
                    ServantModel model = new ServantModel(
                            name.getText().toString().trim(),
                            mobile.getText().toString().trim(),
                            currentAddress.getText().toString().trim(),
                            area.getText().toString().trim(),
                            experience.getText().toString().trim(),
                            aadharNumber.getText().toString().trim(),
                            categorySpinner.getSelectedItem().toString(),
                            genderSpinner.getSelectedItem().toString(),
                            availabilitySpinner.getSelectedItem().toString(),
                            cbVerifyAadhar.isChecked() ? "Verified" : "Unverified"
                    );

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("servants");
                    if (editingServantId != null && submitBtn.getText().toString().equals("Update Servant")) {
                        model.setId(editingServantId);
                        ref.child(editingServantId).setValue(model).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Servant Updated", Toast.LENGTH_SHORT).show();
                                clearForm();
                                submitBtn.setText("Register Servant");
                                editingServantId = null;
                                Log.d(TAG, "Servant updated successfully, ID: " + editingServantId);
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to update servant: " + task.getException());
                            }
                        });
                    } else {
                        String id = ref.push().getKey();
                        if (id != null) {
                            model.setId(id);
                            ref.child(id).setValue(model).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Servant Registered", Toast.LENGTH_SHORT).show();
                                    clearForm();
                                    Log.d(TAG, "Servant registered successfully, ID: " + id);
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to register servant: " + task.getException());
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void filterServants(String query) {
        List<ServantModel> filteredList = new ArrayList<>();
        query = query.toLowerCase().trim();
        for (ServantModel servant : servantList) {
            if (servant != null && (servant.getName().toLowerCase().contains(query) || servant.getArea().toLowerCase().contains(query))) {
                filteredList.add(servant);
            }
        }
        Log.d(TAG, "filterServants: Filtered list size: " + filteredList.size());
        servantAdapter.updateList(filteredList);
    }

    @SuppressLint("SetTextI18n")
    private void populateFormForEdit(ServantModel model) {
        if (name != null) name.setText(model.getName());
        if (mobile != null) mobile.setText(model.getMobile());
        if (currentAddress != null) currentAddress.setText(model.getCurrentAddress());
        if (area != null) area.setText(model.getArea());
        if (experience != null) experience.setText(model.getExperience());
        if (aadharNumber != null) aadharNumber.setText(model.getAadharNumber());
        if (cbVerifyAadhar != null)
            cbVerifyAadhar.setChecked("Verified".equals(model.getVerified()));
        if (aadharVerifiedLayout != null && aadharNumber != null) {
            aadharVerifiedLayout.setVisibility(cbVerifyAadhar != null && cbVerifyAadhar.isChecked() && aadharNumber.getText().toString().matches("^[2-9][0-9]{11}$") ? View.VISIBLE : View.GONE);
        }

        if (categorySpinner != null) {
            for (int i = 0; i < categoryOptions.length; i++) {
                if (categoryOptions[i].equals(model.getCategory())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
        if (genderSpinner != null) {
            for (int i = 0; i < genderOptions.length; i++) {
                if (genderOptions[i].equals(model.getGender())) {
                    genderSpinner.setSelection(i);
                    break;
                }
            }
        }
        if (availabilitySpinner != null) {
            for (int i = 0; i < availabilityOptions.length; i++) {
                if (availabilityOptions[i].equals(model.getAvailability())) {
                    availabilitySpinner.setSelection(i);
                    break;
                }
            }
        }

        if (submitBtn != null) submitBtn.setText("Update Servant");
        Log.d(TAG, "populateFormForEdit: Form populated for servant ID: " + model.getId());
    }

    @SuppressLint("SetTextI18n")
    private void clearForm() {
        if (name != null) name.setText("");
        if (mobile != null) mobile.setText("");
        if (currentAddress != null) currentAddress.setText("");
        if (area != null) area.setText("");
        if (experience != null) experience.setText("");
        if (aadharNumber != null) aadharNumber.setText("");
        if (categorySpinner != null) categorySpinner.setSelection(0);
        if (genderSpinner != null) genderSpinner.setSelection(0);
        if (availabilitySpinner != null) availabilitySpinner.setSelection(0);
        if (cbVerifyAadhar != null) cbVerifyAadhar.setChecked(false);
        if (aadharVerifiedLayout != null) aadharVerifiedLayout.setVisibility(View.GONE);
        if (mobileVerifiedIcon != null) mobileVerifiedIcon.setVisibility(View.GONE);
        if (profilePicture != null) profilePicture.setImageResource(R.drawable.profile);
        if (submitBtn != null) submitBtn.setText("Register Servant");
        editingServantId = null;
        Log.d(TAG, "clearForm: Form cleared");
    }

    private void updateDashboardCounts() {
        int total = servantList != null ? servantList.size() : 0;
        int verified = 0;
        if (servantList != null) {
            for (ServantModel servant : servantList) {
                if (servant != null && "Verified".equalsIgnoreCase(servant.getVerified())) {
                    verified++;
                }
            }
        }
        int unverified = total - verified;

        TextView tvTotalRegistration = findViewById(R.id.tvTotalRegistration);
        TextView tvVerified = findViewById(R.id.tvVerified);
        TextView tvUnverified = findViewById(R.id.tvUnverified);

        if (tvTotalRegistration != null) tvTotalRegistration.setText(String.valueOf(total));
        if (tvVerified != null) tvVerified.setText(String.valueOf(verified));
        if (tvUnverified != null) tvUnverified.setText(String.valueOf(unverified));
        Log.d(TAG, "updateDashboardCounts: Total=" + total + ", Verified=" + verified + ", Unverified=" + unverified);
    }

    private boolean validateInputs() {
        if (name == null || name.getText().toString().trim().isEmpty()) {
            if (name != null) name.setError("Name is required");
            return false;
        }

        String mobileText = mobile != null ? mobile.getText().toString().trim() : "";
        if (mobileText.isEmpty() || !mobileText.matches("^[6-9][0-9]{9}$")) {
            if (mobile != null) mobile.setError("Valid 10-digit mobile number is required");
            return false;
        }

        String experienceText = experience != null ? experience.getText().toString().trim() : "";
        if (experienceText.isEmpty()) {
            if (experience != null) experience.setError("Experience is required");
            return false;
        }

        if (currentAddress == null || currentAddress.getText().toString().trim().isEmpty()) {
            if (currentAddress != null) currentAddress.setError("Address is required");
            return false;
        }

        if (area == null || area.getText().toString().trim().isEmpty()) {
            if (area != null) area.setError("Area is required");
            return false;
        }

        String aadhar = aadharNumber != null ? aadharNumber.getText().toString().trim() : "";
        if (aadhar.isEmpty() || !aadhar.matches("^[2-9][0-9]{11}$")) {
            if (aadharNumber != null)
                aadharNumber.setError("Valid 12-digit Aadhar number is required");
            return false;
        }

        if (categorySpinner == null || categorySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (genderSpinner == null || genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (availabilitySpinner == null || availabilitySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select availability", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
                    if (which == 0) openCamera();
                    else if (which == 1) openGallery();
                    else if (profilePicture != null)
                        profilePicture.setImageResource(R.drawable.profile);
                })
                .show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                }
            } else {
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        } else {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_GALLERY);
        } else {
            Toast.makeText(this, "Gallery app not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == REQUEST_CAMERA) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null && profilePicture != null) {
                        profilePicture.setImageBitmap(photo);
                        Log.d(TAG, "Camera image set");
                    }
                } else if (requestCode == REQUEST_GALLERY) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null && profilePicture != null) {
                        profilePicture.setImageURI(selectedImage);
                        Log.d(TAG, "Gallery image set");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
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
}