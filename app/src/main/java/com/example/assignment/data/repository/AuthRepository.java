package com.example.assignment.data.repository;

import androidx.annotation.Nullable;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.LoginRequest;
import com.example.assignment.data.remote.dto.LoginResponse;
import com.example.assignment.data.remote.dto.RefreshTokenResponse;
import com.example.assignment.data.remote.service.AuthService;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class AuthRepository {

    private final AuthService authService;
    private final SessionManager sessionManager;

    public AuthRepository(ApiServiceFactory serviceFactory, SessionManager sessionManager) {
        this.authService = serviceFactory.createService(AuthService.class);
        this.sessionManager = sessionManager;
    }

    public Result login(String email, String password) {
        Call<LoginResponse> call = authService.login(new LoginRequest(email, password));
        try {
            Response<LoginResponse> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                return Result.error("Đăng nhập thất bại. Mã lỗi: " + response.code());
            }

            LoginResponse body = response.body();
            if (!body.isSuccess()) {
                return Result.error(body.getMessage() != null ? body.getMessage() : "Đăng nhập thất bại");
            }

            String accessToken = body.getAccessToken();
            String refreshToken = body.getRefreshToken();
            String userId = body.getUserId();
            UserRole role = body.getRole();
            Long agencyId = body.getAgencyId();

            sessionManager.saveFullSession(accessToken, refreshToken,
                    new UserSession(userId, role, agencyId));

            return Result.success(new UserSession(userId, role, agencyId));
        } catch (IOException e) {
            return Result.error("Không thể kết nối máy chủ: " + e.getMessage());
        }
    }

    public Result refreshToken() {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            return Result.error("Không có refresh token");
        }

        Call<RefreshTokenResponse> call = authService.refreshToken("Bearer " + refreshToken);
        try {
            Response<RefreshTokenResponse> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                return Result.error("Làm mới token thất bại");
            }

            RefreshTokenResponse body = response.body();
            if (!body.isSuccess()) {
                return Result.error("Làm mới token thất bại");
            }

            String newAccessToken = body.getAccessToken();
            sessionManager.saveTokens(newAccessToken, null);
            return Result.success(null);
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    public void logout() {
        sessionManager.clearSession();
        // Fire and forget logout request
        authService.logout().enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // no-op
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // no-op
            }
        });
    }

    public static class Result {
        private final boolean success;
        @Nullable
        private final UserSession session;
        @Nullable
        private final String error;

        private Result(boolean success, @Nullable UserSession session, @Nullable String error) {
            this.success = success;
            this.session = session;
            this.error = error;
        }

        public static Result success(@Nullable UserSession session) {
            return new Result(true, session, null);
        }

        public static Result error(String message) {
            return new Result(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable
        public UserSession getSession() {
            return session;
        }

        @Nullable
        public String getError() {
            return error;
        }
    }
}


