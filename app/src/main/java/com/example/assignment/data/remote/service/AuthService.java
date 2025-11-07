package com.example.assignment.data.remote.service;

import com.example.assignment.data.remote.dto.LoginRequest;
import com.example.assignment.data.remote.dto.LoginResponse;
import com.example.assignment.data.remote.dto.RefreshTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {

    @POST("/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/auth/token")
    Call<RefreshTokenResponse> refreshToken(@Header("Authorization") String bearerRefreshToken);

    @POST("/auth/logout")
    Call<Void> logout();
}


