import api from './api';


class AuthService {
    signup(userData) {
        return api.post('/auth/signup', userData);
    }

    signin(credentials) {
        return api.post('/auth/signin', credentials);
    }

    logout() {
        localStorage.removeItem('token');
    }
}

export default new AuthService();
