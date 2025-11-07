package com.example.assignment.data.remote.dto.order.manager;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateManagerOrderRequest {

    @SerializedName("orderType")
    private final String orderType;

    @SerializedName("agencyId")
    private final long agencyId;

    @SerializedName("orderItems")
    private final List<OrderItem> orderItems;

    public CreateManagerOrderRequest(String orderType, long agencyId, List<OrderItem> orderItems) {
        this.orderType = orderType;
        this.agencyId = agencyId;
        this.orderItems = orderItems;
    }

    public static class OrderItem {
        @SerializedName("quantity")
        private final int quantity;

        @SerializedName("warehouseId")
        private final long warehouseId;

        @SerializedName("motorbikeId")
        private final long motorbikeId;

        @SerializedName("colorId")
        private final long colorId;

        @SerializedName("discountId")
        private final Long discountId;

        @SerializedName("promotionId")
        private final Long promotionId;

        public OrderItem(int quantity, long warehouseId, long motorbikeId, long colorId,
                         Long discountId, Long promotionId) {
            this.quantity = quantity;
            this.warehouseId = warehouseId;
            this.motorbikeId = motorbikeId;
            this.colorId = colorId;
            this.discountId = discountId;
            this.promotionId = promotionId;
        }
    }
}


