package com.example.assignment.data.remote.dto.order.manager;

import com.example.assignment.data.remote.dto.order.OrderAgencyDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import java.io.Serializable;

public class ManagerOrderSummaryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private long id;

    @SerializedName("status")
    private String status;

    @SerializedName("orderAt")
    private String orderAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("agencyId")
    private Long agencyId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("subtotal")
    private Double subtotal;

    @SerializedName("total")
    private Double total;

    @SerializedName("orderItems")
    private List<ManagerOrderItemDto> orderItems;

    @SerializedName("agency")
    private OrderAgencyDto agency;

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderAt() {
        return orderAt != null ? orderAt : createdAt;
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public int getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public double getSubtotal() {
        if (subtotal != null) {
            return subtotal;
        }
        return total != null ? total : 0d;
    }

    public List<ManagerOrderItemDto> getOrderItems() {
        return orderItems;
    }

    public OrderAgencyDto getAgency() {
        return agency;
    }
}


