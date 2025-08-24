package com.allenhouse.hireeasy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.allenhouse.hireeasy.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class WaveSplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_splash);
        SessionManager sessionManager = new SessionManager(this);

        // Find the custom WaveView and ImageView
        WaveView waveView = findViewById(R.id.wave_view);
        ImageView splashImage = findViewById(R.id.splash_image);

        // Start the wave animation and transition when complete
        waveView.startAnimation(() -> {
            // Fade out the splash image and wave view
            waveView.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                // Start the main activity
                if (sessionManager.isLoggedIn()) {
                    String role = sessionManager.getUserRole();
                    String id = sessionManager.getUserId();

                    Intent intent = null;

                    switch (role) {
                        case "admin":
                            intent = new Intent(WaveSplashActivity.this, AdminDashboard.class);
                            intent.putExtra("admin_id", id);
                            break;
                        case "agent":
                            intent = new Intent(WaveSplashActivity.this, AgentDashboard.class);
                            intent.putExtra("agent_id", id);
                            // Fetch username from Firebase to pass to AgentDashboard
                            Intent finalIntent = intent;
                            FirebaseDatabase.getInstance().getReference().child("agents").child(id)
                                    .child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String username = snapshot.getValue(String.class);
                                            finalIntent.putExtra("username", username != null ? username : "Agent");
                                            startActivity(finalIntent);
                                            finish();
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            ToastUtil.error(WaveSplashActivity.this, "Error fetching username: " + error.getMessage());
                                            startActivity(new Intent(WaveSplashActivity.this, AgentLoginMain.class));
                                            finish();
                                        }
                                    });
                            break;
                    }

                    if (intent != null && !role.equals("agent")) { // Handle non-agent cases here
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } else {
                    startActivity(new Intent(WaveSplashActivity.this, LoginActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }).start();
        });
    }
}