// src/api/axiosConfig.js

import axios from 'axios';

//
// 1) Create an Axios instance with a base URL.
//
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8081/api', // Adjust as needed
});

//
// 2) Request Interceptor
//    - Attach the current access token to each request if available.
//
axiosInstance.interceptors.request.use(
    (config) => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const { accessToken } = JSON.parse(storedUser);
            if (accessToken) {
                config.headers.Authorization = `Bearer ${accessToken}`;
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

//
// 3) Response Interceptor
//    - If we get 401/403, attempt to refresh using the refresh token.
//    - If refresh fails or if it's already the refresh call, log the user out to avoid loops.
//
axiosInstance.interceptors.response.use(
    (response) => response, // pass through if no errors
    async (error) => {
        const originalRequest = error.config;

        // If no response or if we're already calling the refresh endpoint and it fails,
        // we don't want to retry again, or we'll get stuck in a loop.
        if (
            !error.response ||
            originalRequest.url.includes('/users/refresh-token')
        ) {
            // The refresh request itself failed or no response -> log out & stop.
            localStorage.removeItem('user');
            window.location.href = '/login'; // Or however you handle logout
            return Promise.reject(error);
        }

        // src/api/axiosConfig.js (in the .response interceptor)
        if ((error.response.status === 401 || error.response.status === 403) && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
                const oldRefreshToken = storedUser.refreshToken;
                if (!oldRefreshToken) {
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                    return Promise.reject(error);
                }

                // Make sure we pass the oldRefreshToken in the Authorization header
                const refreshResponse = await axios.post(
                    'http://localhost:8081/api/users/refresh-token',
                    null,
                    {
                        headers: {
                            Authorization: `Bearer ${oldRefreshToken}`
                        }
                    }
                );

                // 1) Grab BOTH tokens from the refresh response
                const newAccessToken = refreshResponse.data.accessToken;
                const newRefreshToken = refreshResponse.data.refreshToken;

                // 2) Update localStorage with the brand-new tokens
                storedUser.accessToken = newAccessToken;
                storedUser.refreshToken = newRefreshToken;
                localStorage.setItem('user', JSON.stringify(storedUser));

                // 3) Retry original request with the new access token
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                return axiosInstance(originalRequest);

            } catch (err) {
                // If refresh fails -> log out
                localStorage.removeItem('user');
                window.location.href = '/login';
                return Promise.reject(err);
            }
        }

        // If it's not 401/403, or we've already retried, just reject.
        return Promise.reject(error);
    }
);

export default axiosInstance;
