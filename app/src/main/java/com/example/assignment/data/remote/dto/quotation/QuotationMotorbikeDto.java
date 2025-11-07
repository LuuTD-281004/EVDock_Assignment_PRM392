package com.example.assignment.data.remote.dto.quotation;

import com.google.gson.annotations.SerializedName;

public class QuotationMotorbikeDto {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("model")
    private String model;

    @SerializedName("version")
    private String version;

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
}


