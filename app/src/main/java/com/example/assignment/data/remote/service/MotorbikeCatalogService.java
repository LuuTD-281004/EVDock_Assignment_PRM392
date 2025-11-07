package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface MotorbikeCatalogService {

    @GET("/motorbike")
    Call<ApiResponse<List<MotorbikeDto>>> getMotorbikes(@QueryMap Map<String, Object> params);
}


