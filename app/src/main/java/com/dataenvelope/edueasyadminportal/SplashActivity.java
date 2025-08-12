package com.dataenvelope.edueasyadminportal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION = 2000; // 2 seconds
    private ImageView splashIcon;
    private TextView appName;
    private View loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashIcon = findViewById(R.id.splashIcon);
        appName = findViewById(R.id.appName);
        loadingProgress = findViewById(R.id.loadingProgress);

        // Initially hide the views
        splashIcon.setAlpha(0f);
        appName.setAlpha(0f);
        loadingProgress.setAlpha(0f);

        // Start animations
        startAnimations();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::startMainActivity, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Icon animation
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(splashIcon, "scaleX", 0.5f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(splashIcon, "scaleY", 0.5f, 1f);
        ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(splashIcon, "alpha", 0f, 1f);

        AnimatorSet iconAnimSet = new AnimatorSet();
        iconAnimSet.playTogether(iconScaleX, iconScaleY, iconAlpha);
        iconAnimSet.setDuration(800);
        iconAnimSet.setInterpolator(new DecelerateInterpolator());

        // App name animation
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f);
        ObjectAnimator nameTranslateY = ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f);

        AnimatorSet nameAnimSet = new AnimatorSet();
        nameAnimSet.playTogether(nameAlpha, nameTranslateY);
        nameAnimSet.setDuration(800);
        nameAnimSet.setStartDelay(300);
        nameAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Loading progress animation
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(loadingProgress, "alpha", 0f, 1f);
        progressAlpha.setDuration(500);
        progressAlpha.setStartDelay(600);

        // Play all animations together
        AnimatorSet mainAnimSet = new AnimatorSet();
        mainAnimSet.playTogether(iconAnimSet, nameAnimSet, progressAlpha);
        mainAnimSet.start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}