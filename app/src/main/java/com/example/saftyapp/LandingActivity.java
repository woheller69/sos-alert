package com.example.saftyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isSetupCompleted()) {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

        if (!sessionManager.isWelcomeShown()) {
            showWelcomeDialog(sessionManager);
        }

        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(LandingActivity.this, SetupActivity.class));
            finish();
        });
    }

    private void showWelcomeDialog(SessionManager sessionManager) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_welcome, null);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Get Started", (dialog, which) -> {
                    sessionManager.setWelcomeShown(true);
                    dialog.dismiss();
                })
                .setNegativeButton("About Project", (dialog, which) -> {
                    Intent intent = new Intent(LandingActivity.this, AboutActivity.class);
                    startActivity(intent);
                })
                .show();
    }
}
