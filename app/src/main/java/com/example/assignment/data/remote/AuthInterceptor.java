package com.example.assignment.data.remote;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Attaches the Authorization header to outgoing requests when an access token is available.
 */
public class AuthInterceptor implements Interceptor {

    private final TokenProvider tokenProvider;

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenProvider != null ? tokenProvider.getAccessToken() : null;

        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request authorised = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authorised);
    }
}


