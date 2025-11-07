package com.example.assignment.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.TokenProvider;

/**
 * Handles persistence of auth tokens and basic user metadata.
 */
public class SessionManager implements TokenProvider {

    private static final String PREF_NAME = "evdock_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_AGENCY_ID = "agency_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    @Override
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveTokens(@Nullable String accessToken, @Nullable String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        if (accessToken != null) {
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
        } else {
            editor.remove(KEY_ACCESS_TOKEN);
        }
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_REFRESH_TOKEN);
        }
        editor.apply();
    }

    public void saveUserSession(UserSession session) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, session != null ? session.getUserId() : null);
        editor.putString(KEY_USER_ROLE, session != null ? session.getRole().name() : null);
        if (session != null && session.getAgencyId() != null) {
            editor.putLong(KEY_AGENCY_ID, session.getAgencyId());
        } else {
            editor.remove(KEY_AGENCY_ID);
        }
        editor.apply();
    }

    public void saveFullSession(@Nullable String accessToken,
                                @Nullable String refreshToken,
                                UserSession session) {
        SharedPreferences.Editor editor = prefs.edit();
        if (accessToken != null) {
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
        } else {
            editor.remove(KEY_ACCESS_TOKEN);
        }
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_REFRESH_TOKEN);
        }
        if (session != null) {
            editor.putString(KEY_USER_ID, session.getUserId());
            editor.putString(KEY_USER_ROLE, session.getRole().name());
            if (session.getAgencyId() != null) {
                editor.putLong(KEY_AGENCY_ID, session.getAgencyId());
            } else {
                editor.remove(KEY_AGENCY_ID);
            }
        }
        editor.apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && getUserSession() != null;
    }

    @Nullable
    public UserSession getUserSession() {
        String userId = prefs.getString(KEY_USER_ID, null);
        String roleName = prefs.getString(KEY_USER_ROLE, null);
        if (userId == null || roleName == null) {
            return null;
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            role = UserRole.UNKNOWN;
        }

        Long agencyId = prefs.contains(KEY_AGENCY_ID)
                ? prefs.getLong(KEY_AGENCY_ID, -1L)
                : null;
        if (agencyId != null && agencyId < 0) {
            agencyId = null;
        }

        return new UserSession(userId, role, agencyId);
    }
}


