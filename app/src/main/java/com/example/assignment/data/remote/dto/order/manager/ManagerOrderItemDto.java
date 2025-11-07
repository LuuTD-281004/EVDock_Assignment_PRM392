package com.example.assignment.data.remote.dto.order.manager;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ManagerOrderItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private long id;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("warehouseId")
    private Long warehouseId;

    @SerializedName("motorbikeId")
    private Long motorbikeId;

    @SerializedName("colorId")
    private Long colorId;

    public long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getMotorbikeId() {
        return motorbikeId;
    }

    public Long getColorId() {
        return colorId;
    }
}


