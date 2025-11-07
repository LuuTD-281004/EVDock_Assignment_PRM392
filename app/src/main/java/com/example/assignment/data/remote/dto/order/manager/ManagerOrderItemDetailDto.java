package com.example.assignment.data.remote.dto.order.manager;

import com.example.assignment.data.remote.dto.order.OrderRestockColorDto;
import com.example.assignment.data.remote.dto.order.OrderRestockMotorbikeDto;
import com.example.assignment.data.remote.dto.order.OrderRestockWarehouseDto;
import com.google.gson.annotations.SerializedName;

public class ManagerOrderItemDetailDto {

    @SerializedName("id")
    private long id;

    @SerializedName("orderId")
    private long orderId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("price")
    private Double price;

    @SerializedName("order")
    private ManagerOrderSummaryDto order;

    @SerializedName("motorbike")
    private OrderRestockMotorbikeDto motorbike;

    @SerializedName("warehouse")
    private OrderRestockWarehouseDto warehouse;

    @SerializedName("color")
    private OrderRestockColorDto color;

    public long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
    }

    public int getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public double getPrice() {
        return price != null ? price : 0d;
    }

    public ManagerOrderSummaryDto getOrder() {
        return order;
    }

    public OrderRestockMotorbikeDto getMotorbike() {
        return motorbike;
    }

    public OrderRestockWarehouseDto getWarehouse() {
        return warehouse;
    }

    public OrderRestockColorDto getColor() {
        return color;
    }
}


