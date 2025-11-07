package com.example.assignment.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private Payload data;

    @SerializedName("accessToken")
    private String legacyAccessToken;

    @Nullable
    public String getAccessToken() {
        if (data != null && data.accessToken != null) {
            return data.accessToken;
        }
        return legacyAccessToken;
    }

    public boolean isSuccess() {
        return status != null && status.equalsIgnoreCase("SUCCESS");
    }

    private static class Payload {
        @SerializedName("accessToken")
        String accessToken;
    }
}


