package com.example.assignment.data.remote;

/**
 * Simple abstraction that supplies auth tokens for network layer.
 * The implementation can pull from SharedPreferences, memory cache, etc.
 */
public interface TokenProvider {

    /**
     * @return current access token string or null if user is not authenticated.
     */
    String getAccessToken();

    /**
     * @return current refresh token string or null when unavailable.
     */
    String getRefreshToken();
}


