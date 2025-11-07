package com.example.assignment.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Centralised Retrofit builder. Inject {@link TokenProvider} from application layer.
 */
public final class ApiClient {

    private static final String BASE_URL = "https://evm-project.onrender.com";
    private static final long TIMEOUT_SECONDS = 60L;

    private ApiClient() {
    }

    public static Retrofit create(TokenProvider tokenProvider) {
        OkHttpClient client = buildHttpClient(tokenProvider);

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static OkHttpClient buildHttpClient(TokenProvider tokenProvider) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor);

        if (tokenProvider != null) {
            builder.addInterceptor(new AuthInterceptor(tokenProvider));
        }

        return builder.build();
    }
}


