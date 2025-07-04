package com.allenhouse.servent;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class activity_profile extends AppCompatActivity {

    EditText editName, editMobile, editGmail;
    ImageView profileImage;
    Button submitBtn,changePassBtn;
    private ImageButton Editimage;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        Editimage = findViewById(R.id.btn_edit);
        editName = findViewById(R.id.edit_name);
        editMobile = findViewById(R.id.edit_mobile);
        editGmail = findViewById(R.id.edit_gmail);
        submitBtn = findViewById(R.id.btn_submit);
        changePassBtn = findViewById(R.id.change_pass);

        //  Edit button triggers permission check and image picker
        Editimage.setOnClickListener(view -> {
            checkAndRequestPermissions();
        });

        // ✅ Change password button shows dialog
        changePassBtn.setOnClickListener(v -> {
            showChangePasswordDialog();
        });


        submitBtn.setOnClickListener(view -> {
            String name = editName.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String gmail = editGmail.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty() || gmail.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showChangePasswordDialog() {
        @SuppressLint("InflateParams")
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.change_password, null);

        EditText currentPass = dialogView.findViewById(R.id.edit_current_pass);
        EditText newPass = dialogView.findViewById(R.id.edit_new_pass);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String current = currentPass.getText().toString().trim();
                    String newP = newPass.getText().toString().trim();

                    if (current.isEmpty() || newP.isEmpty()) {
                        Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    } else {
                        // 🔷 Do your password change logic here
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                    else if (profileImage != null)
                        profileImage.setImageResource(R.drawable.profile);
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
                    if (photo != null && profileImage != null) {
                        profileImage.setImageBitmap(photo);
                        Log.d(TAG, "Camera image set");
                    }
                } else if (requestCode == REQUEST_GALLERY) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null && profileImage != null) {
                        profileImage.setImageURI(selectedImage);
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