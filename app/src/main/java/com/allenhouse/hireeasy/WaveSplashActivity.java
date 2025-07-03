package com.allenhouse.hireeasy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class WaveSplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_splash);

        // Find the custom WaveView and ImageView
        WaveView waveView = findViewById(R.id.wave_view);
        ImageView splashImage = findViewById(R.id.splash_image);

        // Start the wave animation and transition when complete
        waveView.startAnimation(() -> {
            // Fade out the splash image and wave view
            waveView.animate().alpha(0f).setDuration(500).start();
            splashImage.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                // Start the main activity
                Intent intent = new Intent(WaveSplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }).start();
        });
    }
}