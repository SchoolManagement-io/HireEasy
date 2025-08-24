package com.allenhouse.hireeasyuser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class WaveSplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_splash);
        SessionManager sessionManager = new SessionManager(this);

        WaveView waveView = findViewById(R.id.wave_view);
        ImageView splashImage = findViewById(R.id.splash_image);

        waveView.startAnimation(() -> {
            waveView.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                if (sessionManager.isLoggedIn()) {
                    String userId = sessionManager.getUserId();
                    String username = sessionManager.getUsername();

                    Intent intent = new Intent(WaveSplashActivity.this, UserDashboard.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(WaveSplashActivity.this, MainActivity.class));
                }
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }).start();
        });
    }
}