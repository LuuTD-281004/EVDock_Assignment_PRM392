package com.example.assignment.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PaginationInfo {

    @SerializedName("page")
    private int page;

    @SerializedName("limit")
    private int limit;

    @SerializedName("total")
    private int total;

    @SerializedName("totalPages")
    private int totalPages;

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }
}


