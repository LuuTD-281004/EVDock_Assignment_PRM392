package com.example.assignment.data.remote.dto.order;

import com.google.gson.annotations.SerializedName;

public class OrderRestockItemDetailDto {

    @SerializedName("id")
    private long id;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("price")
    private Double price;

    @SerializedName("electricMotorbike")
    private OrderRestockMotorbikeDto electricMotorbike;

    @SerializedName("warehouse")
    private OrderRestockWarehouseDto warehouse;

    @SerializedName("color")
    private OrderRestockColorDto color;

    public long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public double getPrice() {
        return price != null ? price : 0d;
    }

    public OrderRestockMotorbikeDto getElectricMotorbike() {
        return electricMotorbike;
    }

    public OrderRestockWarehouseDto getWarehouse() {
        return warehouse;
    }

    public OrderRestockColorDto getColor() {
        return color;
    }
}


