import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const Dashboard = () => {
  const navigate = useNavigate();
  const [activeSection, setActiveSection] = useState('new-interview');

  const menuItems = [
    {
      id: 'new-interview',
      icon: (
        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
        </svg>
      ),
      title: 'Начать новое интервью',
      description: 'Выберите компанию и начните практику с AI-ассистентом',
      route: '/interview-setup'
    },
    {
      id: 'voice-interview',
      icon: (
        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
          <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
        </svg>
      ),
      title: 'Начать голосовое интервью',
      description: 'Практикуйтесь с голосовым AI в реальном времени',
      route: '/voice-interview-setup'
    },
    {
      id: 'progress',
      icon: (
        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
        </svg>
      ),
      title: 'Мой прогресс',
      description: 'Просмотрите историю интервью и отслеживайте улучшения',
      route: '/progress'
    },
    {
      id: 'resume',
      icon: (
        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
          <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
        </svg>
      ),
      title: 'Моё резюме',
      description: 'Управляйте своим резюме и обновляйте информацию',
      route: '/resume'
    },
    {
      id: 'profile',
      icon: (
        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
        </svg>
      ),
      title: 'Профиль',
      description: 'Настройки аккаунта и персональная информация',
      route: '/profile'
    }
  ];

  const handleMenuClick = (item) => {
    setActiveSection(item.id);
    if (item.route) {
      navigate(item.route);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="flex">
        {/* Sidebar */}
        <aside className="w-80 bg-white border-r border-gray-200 min-h-[calc(100vh-64px)] p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-6 px-3">Меню</h2>
          
          <nav className="space-y-2">
            {menuItems.map((item) => (
              <button
                key={item.id}
                onClick={() => handleMenuClick(item)}
                className={`w-full text-left px-4 py-3 rounded-xl transition-all ${
                  activeSection === item.id
                    ? 'bg-[#3D2D4C] text-white shadow-md'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className={activeSection === item.id ? 'text-white' : 'text-[#3D2D4C]'}>
                    {item.icon}
                  </div>
                  <span className="font-medium text-sm">{item.title}</span>
                </div>
              </button>
            ))}
          </nav>

          {/* User info at bottom */}
          <div className="mt-auto pt-8 border-t border-gray-200">
            <div className="px-4 py-3 bg-gray-50 rounded-xl">
              <p className="text-xs text-gray-500 mb-1">Ваш тариф</p>
              <p className="text-sm font-semibold text-gray-900">Бесплатный</p>
              <button className="mt-2 text-xs text-[#3D2D4C] font-medium hover:underline">
                Улучшить план →
              </button>
            </div>
          </div>
        </aside>

        {/* Main content */}
        <main className="flex-1 p-8">
          <div className="max-w-5xl mx-auto">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Личный кабинет</h1>
            <p className="text-gray-600 mb-8">Выберите действие из меню слева</p>
            
            {/* Welcome card */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 mb-6">
              <div className="flex items-start gap-6">
                <div className="w-16 h-16 bg-[#3D2D4C] rounded-2xl flex items-center justify-center flex-shrink-0">
                  <svg className="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
                    <path d="M13 7h-2v5.41l4.29 4.29 1.41-1.41-3.7-3.7V7z"/>
                  </svg>
                </div>
                <div className="flex-1">
                  <h2 className="text-2xl font-bold text-gray-900 mb-2">
                    Готовы начать подготовку?
                  </h2>
                  <p className="text-gray-600 mb-6">
                    Выберите тип интервью и начните практиковаться с нашим AI-ассистентом. 
                    Получайте мгновенную обратную связь и улучшайте свои навыки с каждой сессией.
                  </p>
                  <div className="flex gap-4">
                    <button
                      onClick={() => navigate('/interview-setup')}
                      className="bg-[#3D2D4C] hover:bg-[#2D1D3C] text-white font-semibold px-6 py-3 rounded-xl transition-all shadow-md hover:shadow-lg"
                    >
                      Начать текстовое интервью
                    </button>
                    
                  </div>
                </div>
              </div>
            </div>

            {/* Stats cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                    <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M9 2C6.24 2 4 4.24 4 7v10c0 2.76 2.24 5 5 5h9c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2h-3V5c0-1.66-1.34-3-3-3H9z"/>
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-gray-600">Интервью пройдено</h3>
                </div>
                <p className="text-3xl font-bold text-gray-900">0</p>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                    <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-gray-600">Средний балл</h3>
                </div>
                <p className="text-3xl font-bold text-gray-900">—</p>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 bg-[#3D2D4C] bg-opacity-10 rounded-lg flex items-center justify-center">
                    <svg className="w-5 h-5 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-gray-600">Часов практики</h3>
                </div>
                <p className="text-3xl font-bold text-gray-900">0</p>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default Dashboard;