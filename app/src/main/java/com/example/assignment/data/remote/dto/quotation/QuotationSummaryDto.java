package com.example.assignment.data.remote.dto.quotation;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class QuotationSummaryDto {

    @SerializedName("id")
    private long id;

    @SerializedName("quoteCode")
    private String quoteCode;

    @SerializedName("status")
    private String status;

    @SerializedName("type")
    private String type;

    @SerializedName("basePrice")
    private Double basePrice;

    @SerializedName("promotionPrice")
    private Double promotionPrice;

    @SerializedName("finalPrice")
    private Double finalPrice;

    @SerializedName("createDate")
    private String createDate;

    @SerializedName("validUntil")
    private String validUntil;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("motorbikeId")
    private Long motorbikeId;

    @SerializedName("colorId")
    private Long colorId;

    @SerializedName("dealerStaffId")
    private Long dealerStaffId;

    @SerializedName("agencyId")
    private Long agencyId;

    public long getId() {
        return id;
    }

    public String getQuoteCode() {
        return quoteCode;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public double getBasePrice() {
        return basePrice != null ? basePrice : 0d;
    }

    public double getPromotionPrice() {
        return promotionPrice != null ? promotionPrice : 0d;
    }

    public double getFinalPrice() {
        return finalPrice != null ? finalPrice : 0d;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getValidUntil() {
        return validUntil;
    }

    @Nullable
    public Long getCustomerId() {
        return customerId;
    }

    @Nullable
    public Long getMotorbikeId() {
        return motorbikeId;
    }

    @Nullable
    public Long getColorId() {
        return colorId;
    }

    @Nullable
    public Long getDealerStaffId() {
        return dealerStaffId;
    }

    @Nullable
    public Long getAgencyId() {
        return agencyId;
    }
}


