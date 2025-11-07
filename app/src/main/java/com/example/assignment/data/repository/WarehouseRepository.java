package com.example.assignment.data.repository;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.service.WarehouseService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class WarehouseRepository {

    private final WarehouseService service;

    public WarehouseRepository(ApiServiceFactory factory) {
        this.service = factory.createService(WarehouseService.class);
    }

    public List<WarehouseService.WarehouseDto> getWarehouses() throws IOException {
        Call<ApiResponse<List<WarehouseService.WarehouseDto>>> call = service.getWarehouses();
        Response<ApiResponse<List<WarehouseService.WarehouseDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<WarehouseService.WarehouseDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách kho");
    }
}


