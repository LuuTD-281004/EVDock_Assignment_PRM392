package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.quotation.CreateQuotationRequest;
import com.example.assignment.data.remote.dto.quotation.QuotationDetailDto;
import com.example.assignment.data.remote.dto.quotation.QuotationSummaryDto;
import com.example.assignment.data.remote.dto.quotation.UpdateQuotationRequest;

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

public interface QuotationService {

    @GET("/quotation/list/{agencyId}")
    Call<ApiResponse<List<QuotationSummaryDto>>> getQuotations(
            @Path("agencyId") long agencyId,
            @QueryMap Map<String, Object> params
    );

    @GET("/quotation/detail/{quotationId}")
    Call<ApiResponse<QuotationDetailDto>> getQuotationDetail(@Path("quotationId") long quotationId);

    @POST("/quotation")
    Call<ApiResponse<QuotationDetailDto>> createQuotation(@Body CreateQuotationRequest request);

    @PATCH("/quotation/{quotationId}")
    Call<ApiResponse<QuotationDetailDto>> updateQuotation(
            @Path("quotationId") long quotationId,
            @Body UpdateQuotationRequest request
    );

    @DELETE("/quotation/{quotationId}")
    Call<ApiResponse<Void>> deleteQuotation(@Path("quotationId") long quotationId);
}


