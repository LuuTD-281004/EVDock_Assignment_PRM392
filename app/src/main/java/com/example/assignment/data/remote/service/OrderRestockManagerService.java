package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.order.manager.CreateManagerOrderRequest;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderItemDetailDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface OrderRestockManagerService {

    @GET("/order-restock/list/{agencyId}")
    Call<ApiResponse<List<ManagerOrderSummaryDto>>> getOrders(
            @Path("agencyId") long agencyId,
            @QueryMap Map<String, Object> params
    );

    @POST("/order-restock")
    Call<ApiResponse<ManagerOrderSummaryDto>> createOrder(@Body CreateManagerOrderRequest request);

    @GET("/order-restock/detail/order-item/{orderItemId}")
    Call<ApiResponse<ManagerOrderItemDetailDto>> getOrderItemDetail(@Path("orderItemId") long orderItemId);

    @PATCH("/order-restock/accept/{orderId}")
    Call<ApiResponse<ManagerOrderSummaryDto>> acceptOrder(@Path("orderId") long orderId);

    @DELETE("/order-restock/{orderId}")
    Call<ApiResponse<Void>> deleteOrder(@Path("orderId") long orderId);
}


