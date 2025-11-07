package com.example.assignment.data.remote.dto.order;

import java.util.List;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class OrderRestockDetailDto {

    @SerializedName("id")
    private long id;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("status")
    private String status;

    @SerializedName("orderAt")
    private String orderAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("agencyId")
    private Long agencyId;

    @SerializedName("agency")
    @Nullable
    private OrderAgencyDto agency;

    @SerializedName("itemQuantity")
    private Integer itemQuantity;

    @SerializedName("creditChecked")
    private Boolean creditChecked;

    @SerializedName("subtotal")
    private Double subtotal;

    @SerializedName("total")
    private Double total;

    @SerializedName("orderItems")
    private List<OrderRestockItemDto> orderItems;

    @SerializedName("agencyBill")
    @Nullable
    private OrderRestockInvoiceDto agencyBill;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("paymentType")
    private String paymentType;

    public long getId() {
        return id;
    }

    @Nullable
    public String getOrderCode() {
        return orderCode;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public String getOrderAt() {
        return orderAt != null ? orderAt : createdAt;
    }

    @Nullable
    public Long getAgencyId() {
        return agencyId;
    }

    @Nullable
    public OrderAgencyDto getAgency() {
        return agency;
    }

    public int getItemQuantity() {
        return itemQuantity != null ? itemQuantity : 0;
    }

    public boolean isCreditChecked() {
        return creditChecked != null && creditChecked;
    }

    public double getSubtotal() {
        if (subtotal != null) {
            return subtotal;
        }
        return total != null ? total : 0d;
    }

    public List<OrderRestockItemDto> getOrderItems() {
        return orderItems;
    }

    @Nullable
    public OrderRestockInvoiceDto getAgencyBill() {
        return agencyBill;
    }

    @Nullable
    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Nullable
    public String getPaymentType() {
        return paymentType;
    }
}


