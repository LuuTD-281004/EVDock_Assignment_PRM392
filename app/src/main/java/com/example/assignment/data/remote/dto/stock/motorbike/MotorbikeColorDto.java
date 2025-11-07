package com.example.assignment.data.remote.dto.stock.motorbike;

import com.google.gson.annotations.SerializedName;

public class MotorbikeColorDto {

    @SerializedName("id")
    private long id;

    @SerializedName("colorId")
    private Long colorId;

    @SerializedName("color")
    private MotorbikeColorValueDto color;

    @SerializedName("colorType")
    private String colorType;

    @SerializedName("imageUrl")
    private String imageUrl;

    public long getId() {
        return id;
    }

    public long getColorId() {
        if (colorId != null) {
            return colorId;
        }
        if (color != null) {
            return color.getId();
        }
        return id;
    }

    public String getColorType() {
        if (color != null && color.getColorType() != null) {
            return color.getColorType();
        }
        return colorType;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}


