package com.example.assignment.data.remote.dto;

import androidx.annotation.Nullable;

import com.example.assignment.data.session.UserRole;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("statusCode")
    private Integer statusCode;

    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Payload data;

    // Some environments return fields at root level
    @SerializedName("accessToken")
    private String legacyAccessToken;

    @SerializedName("access_token")
    private String legacyAccessTokenSnake;

    @SerializedName("refreshToken")
    private String legacyRefreshToken;

    @SerializedName("refresh_token")
    private String legacyRefreshTokenSnake;

    @SerializedName("role")
    private JsonElement legacyRoleElement;

    @SerializedName("userId")
    private String legacyUserId;

    @SerializedName("user_id")
    private String legacyUserIdSnake;

    @SerializedName("agencyId")
    private Long legacyAgencyId;

    @SerializedName("agency_id")
    private Long legacyAgencyIdSnake;

    public String getMessage() {
        return message;
    }

    @Nullable
    public String getAccessToken() {
        if (data != null) {
            if (data.accessToken != null) {
                return data.accessToken;
            }
            if (data.accessTokenSnake != null) {
                return data.accessTokenSnake;
            }
        }
        if (legacyAccessToken != null) {
            return legacyAccessToken;
        }
        return legacyAccessTokenSnake;
    }

    @Nullable
    public String getRefreshToken() {
        if (data != null) {
            if (data.refreshToken != null) {
                return data.refreshToken;
            }
            if (data.refreshTokenSnake != null) {
                return data.refreshTokenSnake;
            }
        }
        if (legacyRefreshToken != null) {
            return legacyRefreshToken;
        }
        return legacyRefreshTokenSnake;
    }

    @Nullable
    public String getUserId() {
        if (data != null) {
            if (data.userId != null) {
                return data.userId;
            }
            if (data.userIdSnake != null) {
                return data.userIdSnake;
            }
        }
        if (legacyUserId != null) {
            return legacyUserId;
        }
        return legacyUserIdSnake;
    }

    @Nullable
    public Long getAgencyId() {
        if (data != null) {
            if (data.agencyId != null) {
                return data.agencyId;
            }
            if (data.agencyIdSnake != null) {
                return data.agencyIdSnake;
            }
        }
        if (legacyAgencyId != null) {
            return legacyAgencyId;
        }
        return legacyAgencyIdSnake;
    }

    public UserRole getRole() {
        String apiRole = extractRole(data != null ? data.roleElement : null);
        if (apiRole == null) {
            apiRole = extractRole(legacyRoleElement);
        }
        return UserRole.fromApiRole(apiRole);
    }

    private String extractRole(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonArray() && element.getAsJsonArray().size() > 0) {
            return element.getAsJsonArray().get(0).getAsString();
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return null;
    }

    public boolean isSuccess() {
        if (success != null) {
            return success;
        }
        if (statusCode != null) {
            return statusCode >= 200 && statusCode < 300;
        }
        return status != null && status.equalsIgnoreCase("SUCCESS");
    }

    private static class Payload {
        @SerializedName("accessToken")
        String accessToken;

        @SerializedName("access_token")
        String accessTokenSnake;

        @SerializedName("refreshToken")
        String refreshToken;

        @SerializedName("refresh_token")
        String refreshTokenSnake;

        @SerializedName("role")
        JsonElement roleElement;

        @SerializedName("userId")
        String userId;

        @SerializedName("user_id")
        String userIdSnake;

        @SerializedName("agencyId")
        Long agencyId;

        @SerializedName("agency_id")
        Long agencyIdSnake;
    }
}


