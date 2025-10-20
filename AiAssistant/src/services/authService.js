import api from './api';

/**
 * Сервис для регистрации, входа и выхода пользователя.
 */
class AuthService {
    // Запрос на регистрацию
    signup(userData) {
        return api.post('/auth/signup', userData);
    }

    // Запрос на вход
    signin(credentials) {
        return api.post('/auth/signin', credentials);
    }

    // Выход из системы (просто удаляем токен)
    logout() {
        localStorage.removeItem('token');
        // Здесь можно добавить перенаправление на главную страницу
    }
}

export default new AuthService();
