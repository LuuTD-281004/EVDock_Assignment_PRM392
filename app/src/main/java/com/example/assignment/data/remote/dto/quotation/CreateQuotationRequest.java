package com.example.assignment.data.remote.dto.quotation;

import com.google.gson.annotations.SerializedName;

public class CreateQuotationRequest {

    @SerializedName("type")
    private final String type;

    @SerializedName("basePrice")
    private final double basePrice;

    @SerializedName("promotionPrice")
    private final double promotionPrice;

    @SerializedName("finalPrice")
    private final double finalPrice;

    @SerializedName("validUntil")
    private final String validUntil;

    @SerializedName("customerId")
    private final long customerId;

    @SerializedName("motorbikeId")
    private final long motorbikeId;

    @SerializedName("colorId")
    private final long colorId;

    @SerializedName("dealerStaffId")
    private final long dealerStaffId;

    @SerializedName("agencyId")
    private final long agencyId;

    public CreateQuotationRequest(String type,
                                   double basePrice,
                                   double promotionPrice,
                                   double finalPrice,
                                   String validUntil,
                                   long customerId,
                                   long motorbikeId,
                                   long colorId,
                                   long dealerStaffId,
                                   long agencyId) {
        this.type = type;
        this.basePrice = basePrice;
        this.promotionPrice = promotionPrice;
        this.finalPrice = finalPrice;
        this.validUntil = validUntil;
        this.customerId = customerId;
        this.motorbikeId = motorbikeId;
        this.colorId = colorId;
        this.dealerStaffId = dealerStaffId;
        this.agencyId = agencyId;
    }
}


