package com.example.assignment.data.session;

import androidx.annotation.NonNull;

public enum UserRole {
    EVM_ADMIN("EVM Admin"),
    EVM_STAFF("Evm Staff"),
    DEALER_MANAGER("Dealer Manager"),
    DEALER_STAFF("Dealer Staff"),
    UNKNOWN("Unknown");

    private final String apiValue;

    UserRole(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    @NonNull
    public static UserRole fromApiRole(String apiRole) {
        if (apiRole == null) {
            return UNKNOWN;
        }

        String normalized = apiRole.trim();
        if (normalized.isEmpty()) {
            return UNKNOWN;
        }

        normalized = normalized.replace("ROLE_", "");
        normalized = normalized.replace('_', ' ');

        String lower = normalized.trim().toLowerCase();
        switch (lower) {
            case "admin":
            case "evm admin":
            case "administrator":
                return EVM_ADMIN;
            case "evm staff":
            case "staff":
            case "evm":
                return EVM_STAFF;
            case "dealer manager":
            case "dealermanager":
            case "manager":
                return DEALER_MANAGER;
            case "dealer staff":
            case "dealerstaff":
            case "dealer":
                return DEALER_STAFF;
            default:
                return UNKNOWN;
        }
    }
}


