package com.example.assignment.data.remote.dto.order;

import com.google.gson.annotations.SerializedName;

public class OrderRestockItemDto {

    @SerializedName("id")
    private long id;

    @SerializedName("electricMotorbikeId")
    private Long electricMotorbikeId;

    @SerializedName("warehouseId")
    private Long warehouseId;

    @SerializedName("colorId")
    private Long colorId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("price")
    private Double price;

    @SerializedName("totalPrice")
    private Double totalPrice;

    public long getId() {
        return id;
    }

    public Long getElectricMotorbikeId() {
        return electricMotorbikeId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getColorId() {
        return colorId;
    }

    public int getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public double getPrice() {
        return price != null ? price : 0d;
    }

    public double getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        return getPrice() * getQuantity();
    }
}


