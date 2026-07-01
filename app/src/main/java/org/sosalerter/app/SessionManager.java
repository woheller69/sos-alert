package org.sosalerter.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SOSAlerterPrefs";

    private static final String KEY_USER_NAME = "userName";
    // Alarm styles


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    private static final String KEY_IS_SETUP_COMPLETED = "isSetupCompleted";

    public void setSetupCompleted(boolean completed) {
        editor.putBoolean(KEY_IS_SETUP_COMPLETED, completed);
        editor.apply();
    }

    public boolean isSetupCompleted() {
        return prefs.getBoolean(KEY_IS_SETUP_COMPLETED, false);
    }

    private static final String KEY_WELCOME_SHOWN = "welcomeShown";

    public void setWelcomeShown(boolean shown) {
        editor.putBoolean(KEY_WELCOME_SHOWN, shown);
        editor.apply();
    }

    public boolean isWelcomeShown() {
        return prefs.getBoolean(KEY_WELCOME_SHOWN, false);
    }

    private static final String KEY_EMERGENCY_CALLING_ENABLED = "emergencyCallingEnabled";
    private static final String KEY_EMERGENCY_CALL_PHONE = "emergencyCallPhone";

    public boolean isEmergencyCallingEnabled() {
        return true;
    }

    public void setEmergencyCallPhone(String phone) {
        editor.putString(KEY_EMERGENCY_CALL_PHONE, phone);
        editor.apply();
    }

    public String getEmergencyCallPhone() {
        return prefs.getString(KEY_EMERGENCY_CALL_PHONE, "");
    }


    public int getEmergencyCallDelay() {
        return 1;
    }

    public void saveUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    // TRIGGER SETTERS/GETTERS


    public boolean isTriggerPowerTriple() {
        return true;
    }

    // ALARM PROFILE SETTERS/GETTERS

    public boolean isSirenEnabled() {
        return false;
    }

    public boolean isFlashlightEnabled() {
        return false;
    }

    public boolean isLoudMode() {
        return false;
    }
}
