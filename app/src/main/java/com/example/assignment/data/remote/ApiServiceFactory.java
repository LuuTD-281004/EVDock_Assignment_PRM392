package com.example.assignment.data.remote;

import retrofit2.Retrofit;

/**
 * Provides typed Retrofit service implementations. Reuse a single Retrofit instance per TokenProvider.
 */
public class ApiServiceFactory {

    private final Retrofit retrofit;

    public ApiServiceFactory(TokenProvider tokenProvider) {
        this.retrofit = ApiClient.create(tokenProvider);
    }

    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
}


