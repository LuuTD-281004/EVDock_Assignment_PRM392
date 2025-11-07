package com.example.assignment.data.repository;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.remote.service.MotorbikeCatalogService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class MotorbikeCatalogRepository {

    private final MotorbikeCatalogService service;

    public MotorbikeCatalogRepository(ApiServiceFactory factory) {
        this.service = factory.createService(MotorbikeCatalogService.class);
    }

    public List<MotorbikeDto> getMotorbikes(int limit) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", limit);
        Call<ApiResponse<List<MotorbikeDto>>> call = service.getMotorbikes(params);
        Response<ApiResponse<List<MotorbikeDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<MotorbikeDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách xe");
    }
}


