import axios from 'axios';

// Создаем экземпляр axios с базовой конфигурацией
const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1', // Базовый URL вашего бэкенда
    headers: {
        'Content-Type': 'application/json',
    },
});

// Добавляем interceptor для автоматического добавления токена в заголовки
// Это будет работать для всех запросов после того, как пользователь войдет в систему
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

export default api;
