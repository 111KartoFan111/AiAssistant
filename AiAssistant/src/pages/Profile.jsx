import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const Profile = () => {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  
  // User data state
  const [userData, setUserData] = useState({
    fullName: 'Имя Фамилия',
    email: 'pochta@gmail.com',
    phone: '+7 (777) 123-45-67',
    position: 'Senior Software Engineer',
    company: 'Tech Company',
    experience: '5 лет',
    city: 'Астана',
    about: 'Опытный разработчик с фокусом на backend разработке и системном дизайне.'
  });

  const [editedData, setEditedData] = useState(userData);

  const handleEdit = () => {
    setIsEditing(true);
    setEditedData(userData);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setEditedData(userData);
  };

  const handleSave = async () => {
    setIsSaving(true);
    
    // Simulate API call
    setTimeout(() => {
      setUserData(editedData);
      setIsEditing(false);
      setIsSaving(false);
    }, 1000);
  };

  const handleChange = (field, value) => {
    setEditedData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleLogout = () => {
    // Clear token and navigate to home
    localStorage.removeItem('token');
    navigate('/');
  };

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

        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          {/* Header Section */}
          <div className="bg-[#3D2D4C] px-8 py-8">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-6">
                <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center">
                  <svg className="w-12 h-12 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-white mb-1">
                    {isEditing ? editedData.fullName : userData.fullName}
                  </h1>
                  <p className="text-gray-200">
                    {isEditing ? editedData.position : userData.position}
                  </p>
                  <p className="text-gray-300 text-sm mt-1">
                    {isEditing ? editedData.email : userData.email}
                  </p>
                </div>
              </div>
              <div className="flex gap-3">
                {!isEditing ? (
                  <button
                    onClick={handleEdit}
                    className="bg-white text-[#3D2D4C] px-6 py-2.5 rounded-lg font-semibold hover:bg-gray-100 transition"
                  >
                    Редактировать
                  </button>
                ) : (
                  <>
                    <button
                      onClick={handleCancel}
                      className="bg-transparent border-2 border-white text-white px-6 py-2.5 rounded-lg font-semibold hover:bg-white hover:text-[#3D2D4C] transition"
                    >
                      Отмена
                    </button>
                    <button
                      onClick={handleSave}
                      disabled={isSaving}
                      className="bg-white text-[#3D2D4C] px-6 py-2.5 rounded-lg font-semibold hover:bg-gray-100 transition disabled:opacity-50"
                    >
                      {isSaving ? 'Сохранение...' : 'Сохранить'}
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Content Section */}
          <div className="p-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              {/* Full Name */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Полное имя
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedData.fullName}
                    onChange={(e) => handleChange('fullName', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.fullName}
                  </div>
                )}
              </div>

              {/* Email */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                {isEditing ? (
                  <input
                    type="email"
                    value={editedData.email}
                    onChange={(e) => handleChange('email', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.email}
                  </div>
                )}
              </div>

              {/* Phone */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Телефон
                </label>
                {isEditing ? (
                  <input
                    type="tel"
                    value={editedData.phone}
                    onChange={(e) => handleChange('phone', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.phone}
                  </div>
                )}
              </div>

              {/* City */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Город
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedData.city}
                    onChange={(e) => handleChange('city', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.city}
                  </div>
                )}
              </div>

              {/* Position */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Должность
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedData.position}
                    onChange={(e) => handleChange('position', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.position}
                  </div>
                )}
              </div>

              {/* Company */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Компания
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedData.company}
                    onChange={(e) => handleChange('company', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.company}
                  </div>
                )}
              </div>

              {/* Experience */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Опыт работы
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedData.experience}
                    onChange={(e) => handleChange('experience', e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                  />
                ) : (
                  <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                    {userData.experience}
                  </div>
                )}
              </div>
            </div>

            {/* About */}
            <div className="mb-8">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                О себе
              </label>
              {isEditing ? (
                <textarea
                  value={editedData.about}
                  onChange={(e) => handleChange('about', e.target.value)}
                  rows="4"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition resize-none"
                />
              ) : (
                <div className="px-4 py-3 bg-gray-50 rounded-lg text-gray-900">
                  {userData.about}
                </div>
              )}
            </div>

            {/* Statistics */}
            <div className="border-t border-gray-200 pt-8">
              <h2 className="text-xl font-bold text-gray-900 mb-6">Статистика</h2>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                <div className="bg-gray-50 p-6 rounded-xl">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                      <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M9 2C6.24 2 4 4.24 4 7v10c0 2.76 2.24 5 5 5h9c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2h-3V5c0-1.66-1.34-3-3-3H9z"/>
                      </svg>
                    </div>
                    <span className="text-sm font-medium text-gray-600">Интервью</span>
                  </div>
                  <p className="text-3xl font-bold text-[#3D2D4C]">0</p>
                </div>

                <div className="bg-gray-50 p-6 rounded-xl">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                      <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                      </svg>
                    </div>
                    <span className="text-sm font-medium text-gray-600">Средний балл</span>
                  </div>
                  <p className="text-3xl font-bold text-[#3D2D4C]">—</p>
                </div>

                <div className="bg-gray-50 p-6 rounded-xl">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                      <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/>
                        <path d="M12.5 7H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
                      </svg>
                    </div>
                    <span className="text-sm font-medium text-gray-600">Часов практики</span>
                  </div>
                  <p className="text-3xl font-bold text-[#3D2D4C]">0</p>
                </div>
              </div>
            </div>

            {/* Danger Zone */}
            <div className="border-t border-gray-200 mt-8 pt-8">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Настройки аккаунта</h2>
              <div className="bg-red-50 border border-red-200 rounded-xl p-6">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">Выход из аккаунта</h3>
                    <p className="text-sm text-gray-600">
                      Вы можете выйти из своего аккаунта в любое время
                    </p>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="bg-red-600 hover:bg-red-700 text-white px-6 py-2.5 rounded-lg font-semibold transition"
                  >
                    Выйти
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;