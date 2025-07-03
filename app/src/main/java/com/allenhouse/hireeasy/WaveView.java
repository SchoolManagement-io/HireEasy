package com.allenhouse.hireeasy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

public class WaveView extends View {

    private Paint wavePaint;
    private Path wavePath;
    private float waveHeight = 50f; // Reduced height for smoother waves
    private float waveLength = 700f; // Increased length for fluid wave effect
    private float waveSpeed = 40f; // Speed of wave movement
    private float waveOffset = 0f; // Offset for wave animation
    private float waveProgress = 0f; // Progress of wave (0 = top, 1 = bottom)
    private Runnable onAnimationComplete;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        wavePaint = new Paint();
        wavePaint.setColor(Color.parseColor("#B98A4C"));
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setAntiAlias(true);

        wavePath = new Path();
    }

    public void startAnimation(Runnable onComplete) {
        this.onAnimationComplete = onComplete;
        // Animate wave progress from 0 (top) to 1 (bottom) slowly
        animate()
                .setDuration(2000) // 4 seconds for slower reveal
                .setInterpolator(new AccelerateDecelerateInterpolator()) // Smooth easing
                .setUpdateListener(animation -> {
                    waveProgress = animation.getAnimatedFraction();
                    waveOffset += waveSpeed;
                    invalidate();
                    // Call onComplete when animation finishes
                    if (waveProgress >= 1f && onAnimationComplete != null) {
                        onAnimationComplete.run();
                        onAnimationComplete = null; // Prevent multiple calls
                    }
                }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float startY = height * waveProgress; // Start at top (0), move to bottom (height)

        // Create the wave path
        wavePath.reset();
        wavePath.moveTo(0, startY);
        for (float x = 0; x <= width; x += 5) { // Smaller steps for smoother waves
            float y = startY + (float) Math.sin((x + waveOffset) * 2 * Math.PI / waveLength) * waveHeight;
            wavePath.lineTo(x, y);
        }
        wavePath.lineTo(width, height); // Extend to bottom
        wavePath.lineTo(0, height);
        wavePath.close();

        // Clip the canvas to reveal the image
        canvas.save();
        canvas.clipPath(wavePath);
        canvas.drawColor(Color.WHITE); // Background color behind the image
        canvas.restore();

        // Draw the wave
        canvas.drawPath(wavePath, wavePaint);
    }
}