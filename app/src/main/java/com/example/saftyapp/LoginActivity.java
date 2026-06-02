package com.example.saftyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        MaterialButton btnContinueOffline = findViewById(R.id.btnContinueOffline);

        SessionManager sessionManager = new SessionManager(this);
        btnLogin.setOnClickListener(v -> {
            sessionManager.setLoggedIn(true);
            if (sessionManager.isSetupCompleted()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
            }
            finish();
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnContinueOffline.setOnClickListener(v -> {
            if (sessionManager.isSetupCompleted()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
            }
            finish();
        });
    }
}
