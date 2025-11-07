package com.example.assignment.data.repository;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.order.OrderRestockDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockItemDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockSummaryDto;
import com.example.assignment.data.remote.dto.order.UpdateOrderStatusRequest;
import com.example.assignment.data.remote.service.OrderRestockManagementService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class OrderRestockRepository {

    private final OrderRestockManagementService managementService;

    public OrderRestockRepository(ApiServiceFactory factory) {
        this.managementService = factory.createService(OrderRestockManagementService.class);
    }

    public List<OrderRestockSummaryDto> getOrders(@Nullable String status) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 1000);
        if (status != null && !"all".equalsIgnoreCase(status)) {
            params.put("status", status);
        }

        Call<ApiResponse<List<OrderRestockSummaryDto>>> call = managementService.getOrders(params);
        Response<ApiResponse<List<OrderRestockSummaryDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<OrderRestockSummaryDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách đơn hàng");
    }

    public OrderRestockDetailDto getOrderDetail(long orderId) throws IOException {
        Call<ApiResponse<OrderRestockDetailDto>> call = managementService.getOrderDetail(orderId);
        Response<ApiResponse<OrderRestockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải chi tiết đơn hàng");
    }

    public OrderRestockItemDetailDto getOrderItemDetail(long orderItemId) throws IOException {
        Call<ApiResponse<OrderRestockItemDetailDto>> call = managementService.getOrderItemDetail(orderItemId);
        Response<ApiResponse<OrderRestockItemDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải chi tiết mặt hàng");
    }

    public OrderRestockDetailDto updateStatus(long orderId, String status) throws IOException {
        Call<ApiResponse<OrderRestockDetailDto>> call =
                managementService.updateStatus(orderId, new UpdateOrderStatusRequest(status));
        Response<ApiResponse<OrderRestockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể cập nhật trạng thái");
    }

    public OrderRestockDetailDto checkCredit(long orderId) throws IOException {
        Call<ApiResponse<OrderRestockDetailDto>> call = managementService.checkCredit(orderId);
        Response<ApiResponse<OrderRestockDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể check credit");
    }

    public void deleteOrder(long orderId) throws IOException {
        Call<ApiResponse<Void>> call = managementService.deleteOrder(orderId);
        Response<ApiResponse<Void>> response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException("Không thể xóa đơn hàng");
        }
    }
}


