import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:9000/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to attach the token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // Temporary: Mock User ID for testing until Auth is fully ready
        const userId = localStorage.getItem('userId') || '1';
        config.headers['X-User-Id'] = userId;
        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
