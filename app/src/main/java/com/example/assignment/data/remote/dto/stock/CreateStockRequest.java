package com.example.assignment.data.remote.dto.stock;

import com.google.gson.annotations.SerializedName;

public class CreateStockRequest {

    @SerializedName("agencyId")
    private final long agencyId;

    @SerializedName("motorbikeId")
    private final long motorbikeId;

    @SerializedName("colorId")
    private final long colorId;

    @SerializedName("quantity")
    private final int quantity;

    @SerializedName("price")
    private final double price;

    public CreateStockRequest(long agencyId, long motorbikeId, long colorId, int quantity, double price) {
        this.agencyId = agencyId;
        this.motorbikeId = motorbikeId;
        this.colorId = colorId;
        this.quantity = quantity;
        this.price = price;
    }
}


