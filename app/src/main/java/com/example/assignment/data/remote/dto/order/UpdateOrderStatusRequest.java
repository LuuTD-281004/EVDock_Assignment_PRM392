package com.example.assignment.data.remote.dto.order;

import com.google.gson.annotations.SerializedName;

public class UpdateOrderStatusRequest {

    @SerializedName("status")
    private final String status;

    public UpdateOrderStatusRequest(String status) {
        this.status = status;
    }
}


