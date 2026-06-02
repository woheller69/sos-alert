package com.example.saftyapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saftyapp.data.db.entity.Contact;
import com.example.saftyapp.data.repository.EmergencyRepository;
import com.example.saftyapp.service.EmergencyService;
import com.example.saftyapp.ui.ContactsAdapter;
import com.example.saftyapp.ui.HistoryAdapter;
import com.example.saftyapp.ui.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.net.Uri;
import android.location.LocationManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EmergencyService.ServiceListener {

    private static final int PERMISSIONS_REQUEST_CODE = 999;
    private static final int CONTACT_PICK_REQUEST_CODE = 888;

    private MainViewModel viewModel;
    private SessionManager sessionManager;

    // Service binding
    private EmergencyService emergencyService;
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
    private TextView tvGpsStatus, tvLastEmergency;
    private MaterialButton btnQuickAddContact, btnQuickHistory, btnQuickSettings;

    // History Panel UI
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    // Settings Panel UI
    private EditText etUserNameSetting, etMessageSetting, etSmsInterval;
    private CheckBox cbPowerTriple, cbLongPress, cbVolumeCombo, cbShakeTrigger, cbVoiceTrigger;
    private RadioGroup rgAlertVolume;
    private CheckBox cbSiren, cbFlashlight;
    private EditText etNewContactName, etNewContactPhone;
    private EditText etNewContactPriority;
    private MaterialButton btnAddContact, btnImportContact;
    private RecyclerView rvContacts;
    private ContactsAdapter contactsAdapter;

    // State
    private Contact currentPrimaryContact;
    private int editingContactId = -1;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EmergencyService.LocalBinder binder = (EmergencyService.LocalBinder) service;
            emergencyService = binder.getService();
            isBound = true;
            emergencyService.setListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            emergencyService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Bind Foreground Service
        Intent serviceIntent = new Intent(this, EmergencyService.class);
        startService(serviceIntent); // Ensure service lives independently
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        initializeUI();
        setupNavigation();
        setupSosPanel();
        setupHistoryPanel();
        setupSettingsPanel();
        
        checkAndRequestPermissions();
    }

    private void initializeUI() {
        // Tab layouts
        panelSos = findViewById(R.id.panel_sos);
        panelHistory = findViewById(R.id.panel_history);
        panelSettings = findViewById(R.id.panel_settings);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // SOS tab UI
        btnPanicContainer = findViewById(R.id.btnPanicContainer);
        panicRipple = findViewById(R.id.panic_ripple);
        tvPanicTitle = findViewById(R.id.tvPanicTitle);
        tvPanicSubtitle = findViewById(R.id.tvPanicSubtitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        panicProgress = findViewById(R.id.panic_progress);
        btnCancel = findViewById(R.id.btnCancel);
        btnSafe = findViewById(R.id.btnSafe);
        cardActiveTracking = findViewById(R.id.cardActiveTracking);

        // New SOS Panel UI elements
        tvPrimaryContactDisplay = findViewById(R.id.tvPrimaryContactDisplay);
        layoutPrimaryContactEdit = findViewById(R.id.layoutPrimaryContactEdit);
        etPrimaryName = findViewById(R.id.etPrimaryName);
        etPrimaryPhone = findViewById(R.id.etPrimaryPhone);
        btnEditPrimary = findViewById(R.id.btnEditPrimary);
        btnSavePrimary = findViewById(R.id.btnSavePrimary);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvLastEmergency = findViewById(R.id.tvLastEmergency);
        btnQuickAddContact = findViewById(R.id.btnQuickAddContact);
        btnQuickHistory = findViewById(R.id.btnQuickHistory);
        btnQuickSettings = findViewById(R.id.btnQuickSettings);

        // History tab UI
        rvHistory = findViewById(R.id.rvHistory);

        // Settings tab UI
        etUserNameSetting = findViewById(R.id.etUserNameSetting);
        etMessageSetting = findViewById(R.id.etMessageSetting);
        etSmsInterval = findViewById(R.id.etSmsInterval);

        cbPowerTriple = findViewById(R.id.cbPowerTriple);
        cbLongPress = findViewById(R.id.cbLongPress);
        cbVolumeCombo = findViewById(R.id.cbVolumeCombo);
        cbShakeTrigger = findViewById(R.id.cbShakeTrigger);
        cbVoiceTrigger = findViewById(R.id.cbVoiceTrigger);

        rgAlertVolume = findViewById(R.id.rgAlertVolume);
        cbSiren = findViewById(R.id.cbSiren);
        cbFlashlight = findViewById(R.id.cbFlashlight);

        etNewContactName = findViewById(R.id.etNewContactName);
        etNewContactPhone = findViewById(R.id.etNewContactPhone);
        etNewContactPriority = findViewById(R.id.etNewContactPriority);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnImportContact = findViewById(R.id.btnImportContact);
        rvContacts = findViewById(R.id.rvContacts);
    }

    // TAB NAVIGATION
    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sos) {
                panelSos.setVisibility(View.VISIBLE);
                panelHistory.setVisibility(View.GONE);
                panelSettings.setVisibility(View.GONE);
                return true;
            } else if (id == R.id.nav_history) {
                panelSos.setVisibility(View.GONE);
                panelHistory.setVisibility(View.VISIBLE);
                panelSettings.setVisibility(View.GONE);
                return true;
            } else if (id == R.id.nav_settings) {
                panelSos.setVisibility(View.GONE);
                panelHistory.setVisibility(View.GONE);
                panelSettings.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    // SOS BUTTON WORKFLOW
    private void setupSosPanel() {
        // Main tap activation
        btnPanicContainer.setOnClickListener(v -> {
            if (isBound) {
                if (sessionManager.isTriggerLongPress()) {
                    Toast.makeText(this, "Trigger configured via Long Press!", Toast.LENGTH_SHORT).show();
                } else {
                    emergencyService.startEmergency("SOS Button");
                }
            }
        });

        // Optional long press activation
        btnPanicContainer.setOnLongClickListener(v -> {
            if (sessionManager.isTriggerLongPress() && isBound) {
                emergencyService.startEmergency("Long Press SOS Button");
                return true;
            }
            return false;
        });

        // Cancel Active countdown
        btnCancel.setOnClickListener(v -> {
            if (isBound) {
                emergencyService.stopEmergency();
            }
        });

        // Resolve Safe button
        btnSafe.setOnClickListener(v -> {
            if (isBound) {
                emergencyService.stopEmergency();
            }
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
        viewModel.getLatestSession().observe(this, session -> {
            if (session != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(session.getStartTime()));
                tvLastEmergency.setText("Last SOS Triggered: " + time + " (" + session.getTriggerType() + ")");
            } else {
                tvLastEmergency.setText("Last SOS Triggered: Never");
            }
        });

        // Quick Access Actions
        btnQuickAddContact.setOnClickListener(v -> {
            bottomNavigation.setSelectedItemId(R.id.nav_settings);
            etNewContactName.requestFocus();
        });

        btnQuickHistory.setOnClickListener(v -> {
            bottomNavigation.setSelectedItemId(R.id.nav_history);
        });

        btnQuickSettings.setOnClickListener(v -> {
            bottomNavigation.setSelectedItemId(R.id.nav_settings);
        });
    }

    // LOCAL EMERGENCY HISTORY TIMELINE
    private void setupHistoryPanel() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        // Pass repository to adapter so it can run details loading
        EmergencyRepository repo = new EmergencyRepository(this);
        historyAdapter = new HistoryAdapter(repo);
        rvHistory.setAdapter(historyAdapter);

        // Observe emergency logs
        viewModel.getAllSessions().observe(this, sessions -> {
            if (sessions != null) {
                historyAdapter.setSessions(sessions);
            }
        });
    }

    // CONFIGURATION PANEL SETUP
    private void setupSettingsPanel() {
        // Profile fields
        etUserNameSetting.setText(sessionManager.getUserName());
        etUserNameSetting.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                sessionManager.saveUserName(s.toString().trim());
            }
        });

        etMessageSetting.setText(sessionManager.getCustomMessage());
        etMessageSetting.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                sessionManager.saveCustomMessage(s.toString().trim());
            }
        });

        etSmsInterval.setText(String.valueOf(sessionManager.getSmsIntervalSeconds()));
        etSmsInterval.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString().trim());
                    if (val > 5) {
                        sessionManager.saveSmsIntervalSeconds(val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        // Activation Checkboxes
        cbPowerTriple.setChecked(sessionManager.isTriggerPowerTriple());
        cbPowerTriple.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setTriggerPowerTriple(checked);
            syncServiceSettings();
        });

        cbLongPress.setChecked(sessionManager.isTriggerLongPress());
        cbLongPress.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setTriggerLongPress(checked);
        });

        cbVolumeCombo.setChecked(sessionManager.isTriggerVolumeCombo());
        cbVolumeCombo.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setTriggerVolumeCombo(checked);
            syncServiceSettings();
        });

        cbShakeTrigger.setChecked(sessionManager.isTriggerShake());
        cbShakeTrigger.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setTriggerShake(checked);
            syncServiceSettings();
        });

        cbVoiceTrigger.setChecked(sessionManager.isTriggerVoice());
        cbVoiceTrigger.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setTriggerVoice(checked);
            syncServiceSettings();
        });

        // Siren and Flash Options
        cbSiren.setChecked(sessionManager.isSirenEnabled());
        cbSiren.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setSirenEnabled(checked);
        });

        cbFlashlight.setChecked(sessionManager.isFlashlightEnabled());
        cbFlashlight.setOnCheckedChangeListener((bView, checked) -> {
            sessionManager.setFlashlightEnabled(checked);
        });

        // Loud/Silent selection
        if (sessionManager.isLoudMode()) {
            rgAlertVolume.check(R.id.rbLoud);
        } else {
            rgAlertVolume.check(R.id.rbSilent);
        }
        rgAlertVolume.setOnCheckedChangeListener((group, checkedId) -> {
            sessionManager.setLoudMode(checkedId == R.id.rbLoud);
        });

        // Contacts list management
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter(
            contact -> {
                viewModel.deleteContact(contact);
                if (editingContactId == contact.getId()) {
                    editingContactId = -1;
                    etNewContactName.setText("");
                    etNewContactPhone.setText("");
                    etNewContactPriority.setText("");
                    btnAddContact.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add));
                }
            },
            contact -> {
                editingContactId = contact.getId();
                etNewContactName.setText(contact.getName());
                etNewContactPhone.setText(contact.getPhoneNumber());
                etNewContactPriority.setText(String.valueOf(contact.getPriority()));
                btnAddContact.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_save));
                Toast.makeText(this, "Editing contact: " + contact.getName(), Toast.LENGTH_SHORT).show();
            }
        );
        rvContacts.setAdapter(contactsAdapter);

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
                    tvPrimaryContactDisplay.setText("Primary Contact:\n" + primary.getName() + "\n\nPhone:\n" + primary.getPhoneNumber());
                } else {
                    currentPrimaryContact = null;
                    tvPrimaryContactDisplay.setText("Primary Contact:\nNone Configured\n\nPhone:\nN/A");
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

        // Add / Update contact action
        btnAddContact.setOnClickListener(v -> {
            String name = etNewContactName.getText().toString().trim();
            String phone = etNewContactPhone.getText().toString().trim();
            String priStr = etNewContactPriority.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            int priority = 1;
            if (!priStr.isEmpty()) {
                try {
                    priority = Integer.parseInt(priStr);
                } catch (NumberFormatException ignored) {}
            }

            if (editingContactId != -1) {
                Contact contact = new Contact(name, phone, priority);
                contact.setId(editingContactId);
                viewModel.updateContact(contact);
                editingContactId = -1;
                btnAddContact.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add));
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Contact contact = new Contact(name, phone, priority);
                viewModel.insertContact(contact);
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
            }

            etNewContactName.setText("");
            etNewContactPhone.setText("");
            etNewContactPriority.setText("");
        });

        // Import Contact Action
        btnImportContact.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 101);
            } else {
                pickContact();
            }
        });

        // About SOS Alerter button click listener
        View cardAboutApp = findViewById(R.id.cardAboutApp);
        if (cardAboutApp != null) {
            cardAboutApp.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGpsStatus();
    }

    private void updateGpsStatus() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}

        if (!hasPermission) {
            tvGpsStatus.setText("Current GPS Status: Permission Denied");
            tvGpsStatus.setTextColor(ContextCompat.getColor(this, R.color.panic_red));
        } else if (!gpsEnabled) {
            tvGpsStatus.setText("Current GPS Status: GPS Disabled");
            tvGpsStatus.setTextColor(ContextCompat.getColor(this, R.color.panic_red));
        } else {
            tvGpsStatus.setText("Current GPS Status: Active (High Accuracy)");
            tvGpsStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
        }
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

    private void syncServiceSettings() {
        if (isBound) {
            emergencyService.syncServiceState();
        }
    }

    // EMERGENCY SERVICE EVENT LISTENERS

    @Override
    public void onCountdownTick(int secondsRemaining) {
        runOnUiThread(() -> {
            tvTimer.setText(String.valueOf(secondsRemaining));
            panicProgress.setProgress((10 - secondsRemaining) * 10);
        });
    }

    @Override
    public void onEmergencyStateChanged(boolean isActive, boolean isCountdownCompleted) {
        runOnUiThread(() -> {
            if (isActive) {
                tvStatus.setText(isCountdownCompleted ? "ACTIVE EMERGENCY TRACKING" : "EMERGENCY PROTOCOL STARTED");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.panic_red));
                btnCancel.setVisibility(isCountdownCompleted ? View.GONE : View.VISIBLE);
                btnSafe.setVisibility(isCountdownCompleted ? View.VISIBLE : View.GONE);
                
                tvTimer.setVisibility(isCountdownCompleted ? View.GONE : View.VISIBLE);
                panicProgress.setVisibility(isCountdownCompleted ? View.GONE : View.VISIBLE);
                cardActiveTracking.setVisibility(isCountdownCompleted ? View.VISIBLE : View.GONE);
                
                tvPanicTitle.setText("SOS");
                tvPanicSubtitle.setText(isCountdownCompleted ? "Tracking Active" : "Starting...");

                // Start ripple animations on the panic button during countdown
                if (!isCountdownCompleted) {
                    startRippleAnimation();
                } else {
                    stopRippleAnimation();
                }
            } else {
                tvStatus.setText("System Protected");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
                
                btnCancel.setVisibility(View.GONE);
                btnSafe.setVisibility(View.GONE);
                tvTimer.setVisibility(View.GONE);
                panicProgress.setVisibility(View.GONE);
                cardActiveTracking.setVisibility(View.GONE);
                
                tvPanicTitle.setText("SOS");
                tvPanicSubtitle.setText("Tap to Trigger");
                stopRippleAnimation();
            }
        });
    }

    @Override
    public void onMonitoringStateChanged(boolean isMonitoring) {
        // Monitoring status callback
    }

    // ANIMATIONS
    private void startRippleAnimation() {
        AlphaAnimation anim = new AlphaAnimation(0.2f, 0.9f);
        anim.setDuration(600);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        panicRipple.startAnimation(anim);
        panicRipple.setVisibility(View.VISIBLE);
    }

    private void stopRippleAnimation() {
        panicRipple.clearAnimation();
        panicRipple.setVisibility(View.INVISIBLE);
    }

    // RUNTIME PERMISSIONS

    private void checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        listPermissionsNeeded.add(Manifest.permission.CAMERA);
        listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> listPermissionsToRequest = new ArrayList<>();
        for (String perm : listPermissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsToRequest.add(perm);
            }
        }

        if (!listPermissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean someDenied = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    someDenied = true;
                    break;
                }
            }
            if (someDenied) {
                Toast.makeText(this, "Permissions are required for complete safety activation!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "System protected. Setup completed.", Toast.LENGTH_SHORT).show();
                syncServiceSettings();
            }
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Read contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            emergencyService.setListener(null);
            unbindService(serviceConnection);
            isBound = false;
        }
        if (historyAdapter != null) {
            historyAdapter.releaseMediaPlayer();
        }
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
