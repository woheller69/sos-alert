package org.sosalerter.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);
        MaterialButton btnContinueOffline = findViewById(R.id.btnContinueOffline);

        SessionManager sessionManager = new SessionManager(this);
        btnRegister.setOnClickListener(v -> {
            sessionManager.setLoggedIn(true);
            if (sessionManager.isSetupCompleted()) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
            }
            finishAffinity();
        });

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });

        btnContinueOffline.setOnClickListener(v -> {
            if (sessionManager.isSetupCompleted()) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
            }
            finishAffinity();
        });
    }
}
