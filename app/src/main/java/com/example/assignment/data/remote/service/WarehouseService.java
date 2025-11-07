package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WarehouseService {

    @GET("/warehouses/list")
    Call<ApiResponse<List<WarehouseDto>>> getWarehouses();

    class WarehouseDto {
        public long id;
        public String name;
    }
}


