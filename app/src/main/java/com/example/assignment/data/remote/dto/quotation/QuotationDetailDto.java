package com.example.assignment.data.remote.dto.quotation;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class QuotationDetailDto extends QuotationSummaryDto {

    @SerializedName("customer")
    @Nullable
    private QuotationCustomerDto customer;

    @SerializedName("motorbike")
    @Nullable
    private QuotationMotorbikeDto motorbike;

    @SerializedName("color")
    @Nullable
    private QuotationColorDto color;

    public QuotationCustomerDto getCustomer() {
        return customer;
    }

    public QuotationMotorbikeDto getMotorbike() {
        return motorbike;
    }

    public QuotationColorDto getColor() {
        return color;
    }
}


