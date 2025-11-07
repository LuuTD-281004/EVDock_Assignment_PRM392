package com.example.assignment.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("status")
    private String status;

    @SerializedName("statusCode")
    private Integer statusCode;

    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("paginationInfo")
    private PaginationInfo paginationInfo;

    public boolean isSuccess() {
        if (success != null) {
            return success;
        }
        if (statusCode != null) {
            return statusCode >= 200 && statusCode < 300;
        }
        if (status != null) {
            return status.equalsIgnoreCase("SUCCESS");
        }
        return data != null;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public PaginationInfo getPaginationInfo() {
        return paginationInfo;
    }
}


