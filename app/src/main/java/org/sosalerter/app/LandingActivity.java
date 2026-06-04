package org.sosalerter.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class LandingActivity extends AppCompatActivity {

    private static final int CORE_PERMISSIONS_REQUEST_CODE = 999;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isSetupCompleted()) {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        MaterialButton btnAboutProject = findViewById(R.id.btnAboutProject);

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> showPermissionExplanationDialog());
        }

        if (btnAboutProject != null) {
            btnAboutProject.setOnClickListener(v -> {
                Intent intent = new Intent(LandingActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showPermissionExplanationDialog() {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_permission_explanation, null);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Grant Permissions", (dialog, which) -> {
                    dialog.dismiss();
                    requestCorePermissions();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    // Go to setup even if cancelled (application must launch successfully even when no permissions granted)
                    goToNextScreen();
                })
                .show();
    }

    private void requestCorePermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> listPermissionsToRequest = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsToRequest.add(perm);
            }
        }

        if (!listPermissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsToRequest.toArray(new String[0]), CORE_PERMISSIONS_REQUEST_CODE);
        } else {
            goToNextScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CORE_PERMISSIONS_REQUEST_CODE) {
            goToNextScreen();
        }
    }

    private void goToNextScreen() {
        if (sessionManager.isSetupCompleted()) {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(LandingActivity.this, SetupActivity.class));
        }
        finish();
    }
}
