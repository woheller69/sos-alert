package org.sosalerter.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.sosalerter.app.data.db.entity.Contact;
import org.sosalerter.app.data.repository.EmergencyRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SetupActivity extends AppCompatActivity {

    private TextInputEditText etUserName, etContactName, etContactPhone;
    private MaterialButton btnCompleteSetup;
    private SessionManager sessionManager;
    private EmergencyRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        sessionManager = new SessionManager(this);
        repository = new EmergencyRepository(this);

        // Bind views
        etUserName = findViewById(R.id.etUserName);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnCompleteSetup = findViewById(R.id.btnCompleteSetup);

        btnCompleteSetup.setOnClickListener(v -> completeSetup());
    }

    private void completeSetup() {
        String userName = etUserName.getText().toString().trim();
        String contactName = etContactName.getText().toString().trim();
        String contactPhone = etContactPhone.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contactName.isEmpty()) {
            Toast.makeText(this, "Please enter contact name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contactPhone.isEmpty()) {
            Toast.makeText(this, "Please enter emergency phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save local configs
        sessionManager.saveUserName(userName);
        sessionManager.setSetupCompleted(true);

        // Save primary contact to Room Database
        Contact primaryContact = new Contact(contactName, contactPhone, 1);
        repository.insertContact(primaryContact);

        Toast.makeText(this, "Setup completed successfully!", Toast.LENGTH_SHORT).show();

        // Launch MainActivity and clear history
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
