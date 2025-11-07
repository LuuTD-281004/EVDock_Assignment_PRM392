package com.example.assignment.data.remote.dto.stock;

import com.google.gson.annotations.SerializedName;

public class AgencyStockItemDto {

    @SerializedName("id")
    private long id;

    @SerializedName("agencyId")
    private long agencyId;

    @SerializedName("motorbikeId")
    private long motorbikeId;

    @SerializedName("colorId")
    private Long colorId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    @SerializedName("createAt")
    private String createAt;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() {
        return id;
    }

    public long getAgencyId() {
        return agencyId;
    }

    public long getMotorbikeId() {
        return motorbikeId;
    }

    public Long getColorId() {
        return colorId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getCreateAt() {
        return createAt != null ? createAt : createdAt;
    }
}


