package com.example.assignment.data.remote.dto.stock.motorbike;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MotorbikeDto {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("model")
    private String model;

    @SerializedName("version")
    private String version;

    @SerializedName("makeFrom")
    private String makeFrom;

    @SerializedName("images")
    private List<MotorbikeImageDto> images;

    @SerializedName("colors")
    private List<MotorbikeColorDto> colors;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public String getMakeFrom() {
        return makeFrom;
    }

    public List<MotorbikeImageDto> getImages() {
        return images;
    }

    public List<MotorbikeColorDto> getColors() {
        return colors;
    }
}


