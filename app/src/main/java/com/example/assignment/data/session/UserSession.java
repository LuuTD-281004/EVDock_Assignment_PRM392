package com.example.assignment.data.session;

import androidx.annotation.Nullable;

public class UserSession {

    private final String userId;
    private final UserRole role;
    @Nullable
    private final Long agencyId;

    public UserSession(String userId, UserRole role, @Nullable Long agencyId) {
        this.userId = userId;
        this.role = role == null ? UserRole.UNKNOWN : role;
        this.agencyId = agencyId;
    }

    public String getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    @Nullable
    public Long getAgencyId() {
        return agencyId;
    }
}


