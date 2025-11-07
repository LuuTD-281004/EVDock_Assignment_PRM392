package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.order.OrderRestockDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockItemDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockSummaryDto;
import com.example.assignment.data.remote.dto.order.UpdateOrderStatusRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface OrderRestockManagementService {

    @GET("/order-restock-management/list")
    Call<ApiResponse<List<OrderRestockSummaryDto>>> getOrders(@QueryMap Map<String, Object> params);

    @GET("/order-restock-management/detail/{orderId}")
    Call<ApiResponse<OrderRestockDetailDto>> getOrderDetail(@Path("orderId") long orderId);

    @GET("/order-restock-management/detail/order-item/{orderItemId}")
    Call<ApiResponse<OrderRestockItemDetailDto>> getOrderItemDetail(@Path("orderItemId") long orderItemId);

    @PATCH("/order-restock-management/status/{orderId}")
    Call<ApiResponse<OrderRestockDetailDto>> updateStatus(
            @Path("orderId") long orderId,
            @Body UpdateOrderStatusRequest request
    );

    @PATCH("/order-restock-management/checked/{orderId}")
    Call<ApiResponse<OrderRestockDetailDto>> checkCredit(@Path("orderId") long orderId);

    @DELETE("/order-restock-management/{orderId}")
    Call<ApiResponse<Void>> deleteOrder(@Path("orderId") long orderId);
}


