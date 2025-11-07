package com.example.assignment.data.remote.dto.stock.motorbike;

import com.google.gson.annotations.SerializedName;

public class MotorbikeImageDto {

    @SerializedName("id")
    private long id;

    @SerializedName("imageUrl")
    private String imageUrl;

    public long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}


