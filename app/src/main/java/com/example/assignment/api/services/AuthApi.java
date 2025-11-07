package com.example.assignment.api.services;

import android.os.Handler;
import android.os.Looper;

import com.example.assignment.api.models.LoginRequest;
import com.example.assignment.api.models.LoginResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthApi {
    private static final String BASE_URL = "https://evm-project.onrender.com";
    private static final String LOGIN_ENDPOINT = "/auth/signin";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public AuthApi() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String errorMessage);
    }

    public void login(String email, String password, LoginCallback callback) {
        executorService.execute(() -> {
            try {
                LoginRequest request = new LoginRequest(email, password);
                String json = gson.toJson(request);

                RequestBody body = RequestBody.create(JSON, json);
                Request httpRequest = new Request.Builder()
                        .url(BASE_URL + LOGIN_ENDPOINT)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build();

                try (Response response = client.newCall(httpRequest).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
                        
                        mainHandler.post(() -> callback.onSuccess(loginResponse));
                    } else {
                        String errorMessage = "Đăng nhập thất bại";
                        if (response.body() != null) {
                            try {
                                String errorBody = response.body().string();
                                ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                                if (errorResponse != null && errorResponse.message != null) {
                                    errorMessage = errorResponse.message;
                                }
                            } catch (Exception e) {
                                // Use default error message
                            }
                        }
                        final int statusCode = response.code();
                        if (statusCode == 401) {
                            errorMessage = "Email hoặc mật khẩu không đúng";
                        } else if (statusCode >= 500) {
                            errorMessage = "Lỗi máy chủ. Vui lòng thử lại sau.";
                        }
                        final String finalErrorMessage = errorMessage;
                        mainHandler.post(() -> callback.onError(finalErrorMessage));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet."));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Đã xảy ra lỗi: " + e.getMessage()));
            }
        });
    }

    private static class ErrorResponse {
        String message;
    }
}

