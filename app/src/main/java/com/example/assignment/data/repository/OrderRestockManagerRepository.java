package com.example.assignment.data.repository;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.order.manager.CreateManagerOrderRequest;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderItemDetailDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;
import com.example.assignment.data.remote.service.OrderRestockManagerService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class OrderRestockManagerRepository {

    private final OrderRestockManagerService service;

    public OrderRestockManagerRepository(ApiServiceFactory factory) {
        this.service = factory.createService(OrderRestockManagerService.class);
    }

    public List<ManagerOrderSummaryDto> getOrders(long agencyId, @Nullable String status) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 1000);
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            params.put("status", status);
        }

        Call<ApiResponse<List<ManagerOrderSummaryDto>>> call = service.getOrders(agencyId, params);
        Response<ApiResponse<List<ManagerOrderSummaryDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<ManagerOrderSummaryDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách đơn hàng");
    }

    public ManagerOrderSummaryDto createOrder(CreateManagerOrderRequest request) throws IOException {
        Call<ApiResponse<ManagerOrderSummaryDto>> call = service.createOrder(request);
        Response<ApiResponse<ManagerOrderSummaryDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể tạo đơn hàng");
    }

    public ManagerOrderItemDetailDto getOrderItemDetail(long orderItemId) throws IOException {
        Call<ApiResponse<ManagerOrderItemDetailDto>> call = service.getOrderItemDetail(orderItemId);
        Response<ApiResponse<ManagerOrderItemDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải chi tiết đơn đặt hàng");
    }

    public ManagerOrderSummaryDto acceptOrder(long orderId) throws IOException {
        Call<ApiResponse<ManagerOrderSummaryDto>> call = service.acceptOrder(orderId);
        Response<ApiResponse<ManagerOrderSummaryDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể xác nhận đơn hàng");
    }

    public void deleteOrder(long orderId) throws IOException {
        Call<ApiResponse<Void>> call = service.deleteOrder(orderId);
        Response<ApiResponse<Void>> response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException("Không thể xóa đơn hàng");
        }
    }
}


