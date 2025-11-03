import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import ProfileService from '../services/profileService';

const Profile = () => {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [showPasswordChange, setShowPasswordChange] = useState(false);
  
  // User data state
  const [userData, setUserData] = useState({
    id: '',
    email: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    bio: '',
    avatarBase64: '',
    linkedInProfile: '',
    githubProfile: '',
    createdAt: ''
  });

  const [editedData, setEditedData] = useState({});
  
  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await ProfileService.getMyProfile();
      setUserData(response.data);
      setEditedData(response.data);
      setIsLoading(false);
    } catch (err) {
      setError('Не удалось загрузить профиль');
      console.error('Error loading profile:', err);
      setIsLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
    setEditedData(userData);
    setError('');
    setSuccessMessage('');
  };

  const handleCancel = () => {
    setIsEditing(false);
    setEditedData(userData);
    setError('');
    setSuccessMessage('');
  };

  const handleSave = async () => {
    setIsSaving(true);
    setError('');
    setSuccessMessage('');

    try {
      const updateRequest = {
        firstName: editedData.firstName,
        lastName: editedData.lastName,
        phoneNumber: editedData.phoneNumber,
        bio: editedData.bio,
        linkedInProfile: editedData.linkedInProfile,
        githubProfile: editedData.githubProfile
      };
      
      const response = await ProfileService.updateProfile(updateRequest);
      setUserData(response.data);
      setIsEditing(false);
      setSuccessMessage('Профиль успешно обновлен!');
    } catch (err) {
      setError('Не удалось сохранить изменения');
      console.error('Save error:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleChange = (field, value) => {
    setEditedData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handlePasswordChange = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError('Новые пароли не совпадают');
      return;
    }

    try {
      await ProfileService.changePassword({
        oldPassword: passwordData.oldPassword,
        newPassword: passwordData.newPassword
      });
      setSuccessMessage('Пароль успешно изменен!');
      setShowPasswordChange(false);
      setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setError('Не удалось изменить пароль. Проверьте старый пароль.');
      console.error('Password change error:', err);
    }
  };

  const handleAvatarUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Проверка размера файла (2MB)
    if (file.size > 2 * 1024 * 1024) {
      setError('Размер файла не должен превышать 2MB');
      return;
    }

    try {
      const response = await ProfileService.uploadAvatar(file);
      setUserData(prev => ({ ...prev, avatarBase64: response.data.avatarBase64 }));
      setSuccessMessage('Аватар успешно обновлен!');
    } catch (err) {
      setError('Не удалось загрузить аватар');
      console.error('Avatar upload error:', err);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-6 pt-20 text-center">
          <p className="text-gray-600">Загрузка профиля...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-4xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Назад к Dashboard
        </button>

        {error && (
          <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {successMessage && (
          <div className="mb-4 bg-green-50 border-l-4 border-green-500 p-4 rounded">
            <p className="text-green-700">{successMessage}</p>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          {/* Header Section */}
          <div className="bg-[#3D2D4C] px-8 py-8">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-6">
                <div className="relative">
                  <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center overflow-hidden">
                    {userData.avatarBase64 ? (
                      <img 
                        src={`data:image/jpeg;base64,${userData.avatarBase64}`} 
                        alt="Avatar" 
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <svg className="w-12 h-12 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                      </svg>
                    )}
                  </div>
                  <label className="absolute bottom-0 right-0 bg-blue-500 text-white rounded-full p-2 cursor-pointer hover:bg-blue-600 transition">
                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                    </svg>
                    <input 
                      type="file" 
                      accept="image/*" 
                      className="hidden" 
                      onChange={handleAvatarUpload}
                    />
                  </label>
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-white mb-1">
                    {userData.firstName} {userData.lastName}
                  </h1>
                  <p className="text-gray-200">{userData.email}</p>
                  {userData.createdAt && (
                    <p className="text-gray-300 text-sm mt-1">
                      Член с {new Date(userData.createdAt).toLocaleDateString()}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex flex-col gap-2">
                {isEditing ? (
                  <>
                    <button 
                      onClick={handleSave}
                      disabled={isSaving}
                      className="px-6 py-2 bg-white text-[#3D2D4C] hover:bg-gray-100 rounded-lg font-medium transition disabled:bg-gray-300"
                    >
                      {isSaving ? 'Сохранение...' : 'Сохранить'}
                    </button>
                    <button 
                      onClick={handleCancel}
                      className="px-6 py-2 bg-transparent border-2 border-white text-white hover:bg-white hover:text-[#3D2D4C] rounded-lg font-medium transition"
                    >
                      Отмена
                    </button>
                  </>
                ) : (
                  <button 
                    onClick={handleEdit}
                    className="px-6 py-2 bg-white text-[#3D2D4C] hover:bg-gray-100 rounded-lg font-medium transition"
                  >
                    Редактировать
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Profile Information */}
          <div className="p-8 space-y-6">
            {/* Personal Information */}
            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-4">Личная информация</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Имя</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={editedData.firstName || ''}
                      onChange={(e) => handleChange('firstName', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  ) : (
                    <p className="px-4 py-2 bg-gray-50 rounded-lg">{userData.firstName || 'Не указано'}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Фамилия</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={editedData.lastName || ''}
                      onChange={(e) => handleChange('lastName', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  ) : (
                    <p className="px-4 py-2 bg-gray-50 rounded-lg">{userData.lastName || 'Не указано'}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Телефон</label>
                  {isEditing ? (
                    <input
                      type="tel"
                      value={editedData.phoneNumber || ''}
                      onChange={(e) => handleChange('phoneNumber', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  ) : (
                    <p className="px-4 py-2 bg-gray-50 rounded-lg">{userData.phoneNumber || 'Не указано'}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <p className="px-4 py-2 bg-gray-50 rounded-lg text-gray-500">{userData.email}</p>
                  <p className="text-xs text-gray-500 mt-1">Email нельзя изменить</p>
                </div>
              </div>
            </div>

            {/* Bio */}
            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-4">О себе</h2>
              {isEditing ? (
                <textarea
                  value={editedData.bio || ''}
                  onChange={(e) => handleChange('bio', e.target.value)}
                  rows="4"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  placeholder="Расскажите о себе..."
                />
              ) : (
                <p className="px-4 py-2 bg-gray-50 rounded-lg">{userData.bio || 'Не указано'}</p>
              )}
            </div>

            {/* Social Links */}
            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-4">Социальные сети</h2>
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">LinkedIn</label>
                  {isEditing ? (
                    <input
                      type="url"
                      value={editedData.linkedInProfile || ''}
                      onChange={(e) => handleChange('linkedInProfile', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                      placeholder="https://linkedin.com/in/username"
                    />
                  ) : (
                    <p className="px-4 py-2 bg-gray-50 rounded-lg break-all">
                      {userData.linkedInProfile || 'Не указано'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">GitHub</label>
                  {isEditing ? (
                    <input
                      type="url"
                      value={editedData.githubProfile || ''}
                      onChange={(e) => handleChange('githubProfile', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                      placeholder="https://github.com/username"
                    />
                  ) : (
                    <p className="px-4 py-2 bg-gray-50 rounded-lg break-all">
                      {userData.githubProfile || 'Не указано'}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Password Change Section */}
            <div className="border-t pt-6">
              <button
                onClick={() => setShowPasswordChange(!showPasswordChange)}
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                {showPasswordChange ? '- Скрыть изменение пароля' : '+ Изменить пароль'}
              </button>

              {showPasswordChange && (
                <div className="mt-4 space-y-3 bg-gray-50 p-4 rounded-lg">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Старый пароль</label>
                    <input
                      type="password"
                      value={passwordData.oldPassword}
                      onChange={(e) => setPasswordData(prev => ({ ...prev, oldPassword: e.target.value }))}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Новый пароль</label>
                    <input
                      type="password"
                      value={passwordData.newPassword}
                      onChange={(e) => setPasswordData(prev => ({ ...prev, newPassword: e.target.value }))}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Подтвердите пароль</label>
                    <input
                      type="password"
                      value={passwordData.confirmPassword}
                      onChange={(e) => setPasswordData(prev => ({ ...prev, confirmPassword: e.target.value }))}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  </div>
                  <button
                    onClick={handlePasswordChange}
                    className="px-6 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition"
                  >
                    Изменить пароль
                  </button>
                </div>
              )}
            </div>

            {/* Logout Button */}
            <div className="border-t pt-6">
              <button 
                onClick={handleLogout}
                className="px-6 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg font-medium transition"
              >
                Выйти из аккаунта
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;