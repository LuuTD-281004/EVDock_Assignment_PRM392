package com.example.assignment.data.remote.dto.stock.motorbike;

import com.google.gson.annotations.SerializedName;

public class MotorbikeColorValueDto {

    @SerializedName("id")
    private long id;

    @SerializedName("colorType")
    private String colorType;

    public long getId() {
        return id;
    }

    public String getColorType() {
        return colorType;
    }
}


