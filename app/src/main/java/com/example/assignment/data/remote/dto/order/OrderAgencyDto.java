package com.example.assignment.data.remote.dto.order;

import com.google.gson.annotations.SerializedName;

public class OrderAgencyDto {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}


