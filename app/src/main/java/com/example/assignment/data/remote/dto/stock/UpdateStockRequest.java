package com.example.assignment.data.remote.dto.stock;

import com.google.gson.annotations.SerializedName;

public class UpdateStockRequest {

    @SerializedName("quantity")
    private final int quantity;

    @SerializedName("price")
    private final double price;

    public UpdateStockRequest(int quantity, double price) {
        this.quantity = quantity;
        this.price = price;
    }
}


