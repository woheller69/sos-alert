package org.sosalerter.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.google.android.material.materialswitch.MaterialSwitch;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.sosalerter.app.data.db.entity.Contact;
import org.sosalerter.app.data.repository.EmergencyRepository;
import org.sosalerter.app.ui.ContactsAdapter;
import org.sosalerter.app.ui.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 999;
    private static final int CONTACT_PICK_REQUEST_CODE = 888;

    private MainViewModel viewModel;
    private SessionManager sessionManager;

    // Service binding
    private boolean isBound = false;

    // UI Panels
    private View panelSos;
    private View panelHistory;
    private View panelSettings;
    private BottomNavigationView bottomNavigation;

    // SOS Panel UI
    private View btnPanicContainer;
    private View panicRipple;
    private TextView tvPanicTitle;
    private TextView tvPanicSubtitle;
    private TextView tvStatus, tvTimer;
    private ProgressBar panicProgress;
    private MaterialButton btnCancel, btnSafe;
    private View cardActiveTracking;

    // New SOS Panel UI elements
    private TextView tvPrimaryContactDisplay;
    private View layoutPrimaryContactEdit;
    private EditText etPrimaryName, etPrimaryPhone;
    private MaterialButton btnEditPrimary, btnSavePrimary;
    private TextView tvLastEmergency;
    private MaterialButton btnQuickAddContact, btnQuickHistory, btnQuickSettings;

    // Settings Panel UI
    private EditText etUserNameSetting;
    private EditText etNewContactName, etNewContactPhone;
    private EditText etNewContactPriority;
    private RecyclerView rvContacts;
    private ContactsAdapter contactsAdapter;

    // State
    private Contact currentPrimaryContact;
    private int editingContactId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initializeUI();
        setupNavigation();
        setupSosPanel();
        setupSettingsPanel();
    }

    private void initializeUI() {
        // Tab layouts
        panelSos = findViewById(R.id.panel_sos);
        panelHistory = findViewById(R.id.panel_history);
        panelSettings = findViewById(R.id.panel_settings);

        // SOS tab UI
        btnPanicContainer = findViewById(R.id.btnPanicContainer);
        panicRipple = findViewById(R.id.panic_ripple);
        tvPanicTitle = findViewById(R.id.tvPanicTitle);
        tvPanicSubtitle = findViewById(R.id.tvPanicSubtitle);
        tvTimer = findViewById(R.id.tvTimer);
        panicProgress = findViewById(R.id.panic_progress);
        cardActiveTracking = findViewById(R.id.cardActiveTracking);

        // New SOS Panel UI elements
        tvPrimaryContactDisplay = findViewById(R.id.tvPrimaryContactDisplay);
        layoutPrimaryContactEdit = findViewById(R.id.layoutPrimaryContactEdit);
        etPrimaryName = findViewById(R.id.etPrimaryName);
        etPrimaryPhone = findViewById(R.id.etPrimaryPhone);
        btnEditPrimary = findViewById(R.id.btnEditPrimary);
        btnSavePrimary = findViewById(R.id.btnSavePrimary);

        // Settings tab UI
        etUserNameSetting = findViewById(R.id.etUserNameSetting);

        etNewContactName = findViewById(R.id.etNewContactName);
        etNewContactPhone = findViewById(R.id.etNewContactPhone);
        etNewContactPriority = findViewById(R.id.etNewContactPriority);
        rvContacts = findViewById(R.id.rvContacts);
    }

    // TAB NAVIGATION
    private void setupNavigation() {
        if (bottomNavigation == null) return;
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sos) {
                if (panelSos != null) panelSos.setVisibility(View.VISIBLE);
                if (panelHistory != null) panelHistory.setVisibility(View.GONE);
                if (panelSettings != null) panelSettings.setVisibility(View.GONE);
                return true;
            } else if (id == R.id.nav_settings) {
                if (panelSos != null) panelSos.setVisibility(View.GONE);
                if (panelHistory != null) panelHistory.setVisibility(View.GONE);
                if (panelSettings != null) panelSettings.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    private void triggerCallIfEnabled() {
        String phone = sessionManager.getEmergencyCallPhone();
        makeEmergencyCall(phone);
    }

    private void makeEmergencyCall(String phone) {
        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phone));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to place call automatically", e);
            }
        } else {
            Log.w(TAG, "CALL_PHONE permission not granted. Cannot place automatic call.");
        }
    }
    // SOS BUTTON WORKFLOW
    private void setupSosPanel() {
        // Main tap activation
        btnPanicContainer.setOnClickListener(v -> {
            triggerCallIfEnabled();
        });

        // Edit Primary Contact
        btnEditPrimary.setOnClickListener(v -> {
            if (layoutPrimaryContactEdit.getVisibility() == View.GONE) {
                layoutPrimaryContactEdit.setVisibility(View.VISIBLE);
                btnSavePrimary.setVisibility(View.VISIBLE);
                btnEditPrimary.setText("Cancel");
                if (currentPrimaryContact != null) {
                    etPrimaryName.setText(currentPrimaryContact.getName());
                    etPrimaryPhone.setText(currentPrimaryContact.getPhoneNumber());
                } else {
                    etPrimaryName.setText("");
                    etPrimaryPhone.setText("");
                }
            } else {
                layoutPrimaryContactEdit.setVisibility(View.GONE);
                btnSavePrimary.setVisibility(View.GONE);
                btnEditPrimary.setText("Edit Contact");
            }
        });

        // Save Primary Contact
        btnSavePrimary.setOnClickListener(v -> {
            String name = etPrimaryName.getText().toString().trim();
            String phone = etPrimaryPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPrimaryContact != null) {
                currentPrimaryContact.setName(name);
                currentPrimaryContact.setPhoneNumber(phone);
                viewModel.updateContact(currentPrimaryContact);
                Toast.makeText(this, "Primary contact updated", Toast.LENGTH_SHORT).show();
            } else {
                Contact newPrimary = new Contact(name, phone, 1);
                viewModel.insertContact(newPrimary);
                Toast.makeText(this, "Primary contact saved", Toast.LENGTH_SHORT).show();
            }

            layoutPrimaryContactEdit.setVisibility(View.GONE);
            btnSavePrimary.setVisibility(View.GONE);
            btnEditPrimary.setText("Edit Contact");
        });

        // Observe latest session for timestamp
        viewModel.getLatestSession().observe(this, session -> {});

    }


    // CONFIGURATION PANEL SETUP
    private void setupSettingsPanel() {
        if (etUserNameSetting != null) {
            etUserNameSetting.setText(sessionManager.getUserName());
            etUserNameSetting.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    sessionManager.saveUserName(s.toString().trim());
                }
            });
        }

        // Contacts list management
        if (rvContacts != null) {
            rvContacts.setLayoutManager(new LinearLayoutManager(this));
            contactsAdapter = new ContactsAdapter(
                contact -> {
                    viewModel.deleteContact(contact);
                    if (editingContactId == contact.getId()) {
                        editingContactId = -1;
                        if (etNewContactName != null) etNewContactName.setText("");
                        if (etNewContactPhone != null) etNewContactPhone.setText("");
                        if (etNewContactPriority != null) etNewContactPriority.setText("");
                                            }
                },
                contact -> {
                    editingContactId = contact.getId();
                    if (etNewContactName != null) etNewContactName.setText(contact.getName());
                    if (etNewContactPhone != null) etNewContactPhone.setText(contact.getPhoneNumber());
                    if (etNewContactPriority != null) etNewContactPriority.setText(String.valueOf(contact.getPriority()));
                    Toast.makeText(this, "Editing contact: " + contact.getName(), Toast.LENGTH_SHORT).show();
                }
            );
            rvContacts.setAdapter(contactsAdapter);
        }

        // Observe contacts list
        viewModel.getAllContacts().observe(this, contacts -> {
            if (contacts != null) {
                contactsAdapter.setContacts(contacts);
                
                // Update primary contact display
                Contact primary = null;
                for (Contact c : contacts) {
                    if (c.getPriority() == 1) {
                        primary = c;
                        break;
                    }
                }
                
                // Fallback to first contact if no priority 1
                if (primary == null && !contacts.isEmpty()) {
                    primary = contacts.get(0);
                }

                if (primary != null) {
                    currentPrimaryContact = primary;
                    tvPrimaryContactDisplay.setText(getString(R.string.name)+":\n"+primary.getName() + "\n\n"+getString(R.string.phone)+":\n" + primary.getPhoneNumber());
                } else {
                    currentPrimaryContact = null;
                    tvPrimaryContactDisplay.setText(getString(R.string.name)+":\n "+"None Configured\n\n"+getString(R.string.phone)+":\nN/A");
                }
                
                // Pre-populate priority hint if adding new contact
                if (editingContactId == -1) {
                    int nextPriority = 1;
                    for (Contact c : contacts) {
                        if (c.getPriority() >= nextPriority) {
                            nextPriority = c.getPriority() + 1;
                        }
                    }
                    etNewContactPriority.setText(String.valueOf(nextPriority));
                }
            }
        });

        // Emergency Calling controls setup
        MaterialSwitch cbEnableCalling = findViewById(R.id.cbEnableCalling);

            cbEnableCalling.setChecked(sessionManager.isEmergencyCallingEnabled());
            // Populate spCallingContact dynamically from live contacts list
            viewModel.getAllContacts().observe(this, contacts -> {
                if (contacts != null) {
                    List<String> contactNames = new ArrayList<>();
                    for (Contact c : contacts) {
                        contactNames.add(c.getName() + " (" + c.getPhoneNumber() + ")");
                    }

                    if (contactNames.isEmpty()) {
                        contactNames.add("No Contacts Available");
                        cbEnableCalling.setEnabled(false);
                        cbEnableCalling.setChecked(false);
                    } else {
                        cbEnableCalling.setEnabled(true);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, contactNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    String savedPhone = sessionManager.getEmergencyCallPhone();
                    if (!contacts.isEmpty()) {
                        sessionManager.setEmergencyCallPhone(contacts.get(0).getPhoneNumber());
                    }
                }
            });

    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String[] projection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "";
                        String phone = phoneIndex >= 0 ? cursor.getString(phoneIndex) : "";

                        etNewContactName.setText(name);
                        etNewContactPhone.setText(phone);
                        etNewContactPriority.requestFocus();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to read contact details", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
