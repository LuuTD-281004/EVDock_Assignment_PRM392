package com.example.assignment.data.remote.dto.order;

import com.google.gson.annotations.SerializedName;

public class OrderRestockInvoiceDto {

    @SerializedName("id")
    private long id;

    @SerializedName("paymentAmount")
    private Double paymentAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("agency")
    private OrderAgencyDto agency;

    public long getId() {
        return id;
    }

    public double getPaymentAmount() {
        return paymentAmount != null ? paymentAmount : 0d;
    }

    public String getStatus() {
        return status;
    }

    public OrderAgencyDto getAgency() {
        return agency;
    }
}


