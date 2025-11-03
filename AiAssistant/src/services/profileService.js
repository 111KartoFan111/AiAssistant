import api from './api';

class ProfileService {
    // Получить профиль текущего пользователя
    getMyProfile() {
        return api.get('/profile');
    }

    // Обновить профиль
    updateProfile(profileData) {
        return api.put('/profile', profileData);
    }

    // Загрузить аватар
    uploadAvatar(file) {
        const formData = new FormData();
        formData.append('file', file);
        
        return api.post('/profile/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    }

    // Изменить пароль
    changePassword(passwordData) {
        return api.put('/profile/password', passwordData);
    }

    // Удалить аккаунт
    deleteAccount() {
        return api.delete('/profile');
    }
}

export default new ProfileService();