package com.example.assignment.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "EVDockPrefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_AGENCY_ID = "agency_id";
    private static final String KEY_SAVED_CREDENTIALS = "saved_credentials";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Token management
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void saveUserRole(String role) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

    public void saveAgencyId(String agencyId) {
        prefs.edit().putString(KEY_AGENCY_ID, agencyId).apply();
    }

    public String getAgencyId() {
        return prefs.getString(KEY_AGENCY_ID, null);
    }

    // Saved credentials for quick login
    public static class SavedCredential {
        public String email;
        public String password;
        public String label;

        public SavedCredential(String email, String password, String label) {
            this.email = email;
            this.password = password;
            this.label = label;
        }
    }

    public void saveCredentials(List<SavedCredential> credentials) {
        String json = gson.toJson(credentials);
        prefs.edit().putString(KEY_SAVED_CREDENTIALS, json).apply();
    }

    public List<SavedCredential> getSavedCredentials() {
        String json = prefs.getString(KEY_SAVED_CREDENTIALS, null);
        if (json == null) {
            // Initialize with default credentials
            List<SavedCredential> defaultCreds = new ArrayList<>();
            defaultCreds.add(new SavedCredential("admin@gmail.com", "123456789", "Admin"));
            defaultCreds.add(new SavedCredential("vyvy@gmail.com", "123456789", "Dealer Staff - Agency 1"));
            defaultCreds.add(new SavedCredential("john.doe@email.com", "securePassword123", "Dealer Manager"));
            defaultCreds.add(new SavedCredential("evmstaff@gmail.com", "123456789", "EVM Staff"));
            saveCredentials(defaultCreds);
            return defaultCreds;
        }
        Type type = new TypeToken<List<SavedCredential>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}

