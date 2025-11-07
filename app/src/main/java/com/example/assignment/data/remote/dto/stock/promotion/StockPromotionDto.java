package com.example.assignment.data.remote.dto.stock.promotion;

import com.google.gson.annotations.SerializedName;

public class StockPromotionDto {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("valueType")
    private String valueType;

    @SerializedName("value")
    private double value;

    @SerializedName("status")
    private String status;

    @SerializedName("startAt")
    private String startAt;

    @SerializedName("endAt")
    private String endAt;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValueType() {
        return valueType;
    }

    public double getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }
}


