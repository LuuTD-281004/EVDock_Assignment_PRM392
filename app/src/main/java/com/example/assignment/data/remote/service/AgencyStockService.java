package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.remote.dto.stock.CreateStockRequest;
import com.example.assignment.data.remote.dto.stock.UpdateStockRequest;

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

public interface AgencyStockService {

    @POST("/agency-stock")
    Call<ApiResponse<AgencyStockDetailDto>> createStock(@Body CreateStockRequest request);

    @GET("/agency-stock/list/{agencyId}")
    Call<ApiResponse<List<AgencyStockItemDto>>> getStocks(
            @Path("agencyId") long agencyId,
            @QueryMap Map<String, Object> params
    );

    @GET("/agency-stock/detail/{stockId}")
    Call<ApiResponse<AgencyStockDetailDto>> getStockDetail(@Path("stockId") long stockId);

    @PATCH("/agency-stock/{stockId}")
    Call<ApiResponse<AgencyStockDetailDto>> updateStock(
            @Path("stockId") long stockId,
            @Body UpdateStockRequest request
    );

    @DELETE("/agency-stock/{stockId}")
    Call<ApiResponse<Void>> deleteStock(@Path("stockId") long stockId);
}


