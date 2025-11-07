package com.example.assignment.data.repository;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.ApiResponse;
import com.example.assignment.data.remote.dto.quotation.CreateQuotationRequest;
import com.example.assignment.data.remote.dto.quotation.QuotationDetailDto;
import com.example.assignment.data.remote.dto.quotation.QuotationSummaryDto;
import com.example.assignment.data.remote.dto.quotation.UpdateQuotationRequest;
import com.example.assignment.data.remote.service.QuotationService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class QuotationRepository {

    private final QuotationService service;

    public QuotationRepository(ApiServiceFactory factory) {
        this.service = factory.createService(QuotationService.class);
    }

    public List<QuotationSummaryDto> getQuotations(long agencyId,
                                                   @Nullable String status,
                                                   @Nullable String type,
                                                   @Nullable String search) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 1000);
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            params.put("status", status);
        }
        if (type != null && !type.equalsIgnoreCase("ALL")) {
            params.put("type", type);
        }
        if (search != null && !search.trim().isEmpty()) {
            params.put("quoteCode", search.trim());
        }

        Call<ApiResponse<List<QuotationSummaryDto>>> call = service.getQuotations(agencyId, params);
        Response<ApiResponse<List<QuotationSummaryDto>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            List<QuotationSummaryDto> data = response.body().getData();
            return data != null ? data : Collections.emptyList();
        }
        throw new IOException("Không thể tải danh sách báo giá");
    }

    public QuotationDetailDto getQuotationDetail(long quotationId) throws IOException {
        Call<ApiResponse<QuotationDetailDto>> call = service.getQuotationDetail(quotationId);
        Response<ApiResponse<QuotationDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException("Không thể tải thông tin báo giá");
    }

    public QuotationDetailDto createQuotation(CreateQuotationRequest request) throws IOException {
        Call<ApiResponse<QuotationDetailDto>> call = service.createQuotation(request);
        Response<ApiResponse<QuotationDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể tạo báo giá");
    }

    public QuotationDetailDto updateQuotation(long quotationId, UpdateQuotationRequest request) throws IOException {
        Call<ApiResponse<QuotationDetailDto>> call = service.updateQuotation(quotationId, request);
        Response<ApiResponse<QuotationDetailDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return response.body().getData();
        }
        throw new IOException(response.body() != null ? response.body().getMessage() : "Không thể cập nhật báo giá");
    }

    public void deleteQuotation(long quotationId) throws IOException {
        Call<ApiResponse<Void>> call = service.deleteQuotation(quotationId);
        Response<ApiResponse<Void>> response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException("Không thể xóa báo giá");
        }
    }
}


