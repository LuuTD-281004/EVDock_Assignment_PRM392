package com.example.assignment.data.repository;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.remote.dto.stock.CreateStockRequest;
import com.example.assignment.data.remote.dto.stock.UpdateStockRequest;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.remote.service.AgencyStockService;
import com.example.assignment.data.remote.service.MotorbikeService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class StockRepository {

    private final AgencyStockService stockService;
    private final MotorbikeService motorbikeService;

    public StockRepository(ApiServiceFactory serviceFactory) {
        this.stockService = serviceFactory.createService(AgencyStockService.class);
        this.motorbikeService = serviceFactory.createService(MotorbikeService.class);
    }

    public List<AgencyStockItemDto> getStocks(long agencyId,
                                              @Nullable Long motorbikeId,
                                              @Nullable Long colorId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 1000);
        if (motorbikeId != null) {
            params.put("motorbikeId", motorbikeId);
        }
        if (colorId != null) {
            params.put("colorId", colorId);
        }

        Call<ApiResponse<List<AgencyStockItemDto>>> call = stockService.getStocks(agencyId, params);
        Response<ApiResponse<List<AgencyStockItemDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<AgencyStockItemDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách tồn kho");
    }

    public AgencyStockDetailDto getStockDetail(long stockId) throws IOException {
        Call<ApiResponse<AgencyStockDetailDto>> call = stockService.getStockDetail(stockId);
        Response<ApiResponse<AgencyStockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải chi tiết tồn kho");
    }

    public AgencyStockDetailDto createStock(long agencyId,
                                            long motorbikeId,
                                            long colorId,
                                            int quantity,
                                            double price) throws IOException {
        CreateStockRequest request = new CreateStockRequest(agencyId, motorbikeId, colorId, quantity, price);
        Call<ApiResponse<AgencyStockDetailDto>> call = stockService.createStock(request);
        Response<ApiResponse<AgencyStockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể tạo tồn kho");
    }

    public AgencyStockDetailDto updateStock(long stockId,
                                            int quantity,
                                            double price) throws IOException {
        UpdateStockRequest request = new UpdateStockRequest(quantity, price);
        Call<ApiResponse<AgencyStockDetailDto>> call = stockService.updateStock(stockId, request);
        Response<ApiResponse<AgencyStockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể cập nhật tồn kho");
    }

    public boolean deleteStock(long stockId) throws IOException {
        Call<ApiResponse<Void>> call = stockService.deleteStock(stockId);
        Response<ApiResponse<Void>> response = call.execute();
        if (response.isSuccessful()) {
            return true;
        }
        throw new IOException("Không thể xóa tồn kho");
    }

    public List<MotorbikeDto> getMotorbikes(int limit) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("page", 1);
        Call<ApiResponse<List<MotorbikeDto>>> call = motorbikeService.getMotorbikes(params);
        Response<ApiResponse<List<MotorbikeDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<MotorbikeDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách xe");
    }

    public MotorbikeDto getMotorbikeDetail(long motorbikeId) throws IOException {
        Call<ApiResponse<MotorbikeDto>> call = motorbikeService.getMotorbikeDetail(motorbikeId);
        Response<ApiResponse<MotorbikeDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải thông tin xe");
    }
}


