import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import AuthService from '../services/authService';

const SignUp = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Сбрасываем ошибку перед новым запросом

    try {
      await AuthService.signup({ fullName: name, email, password });
      // Если регистрация прошла успешно, перенаправляем на страницу входа
      navigate('/signin');
    } catch (err) {
      // Обрабатываем ошибку
      if (err.response && err.response.data) {
        setError(err.response.data.message || 'Registration failed. Please try again.');
      } else {
        setError('Registration failed. Please check your connection.');
      }
      console.error('Signup error:', err);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-md mx-auto px-6 pt-20 pb-20">
        <div className="bg-white rounded-2xl shadow-lg p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Создать аккаунт</h1>
          <p className="text-gray-600 mb-8">Начните подготовку к собеседованиям</p>
          
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Полное имя
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                placeholder="Полное имя"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                placeholder="your@email.com"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Пароль
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                placeholder="••••••••"
                required
              />
            </div>
            
            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <button
              type="submit"
              className="w-full bg-[#3D2D4C] hover:bg-[#2D1D3C] text-white font-semibold py-3 rounded-lg transition shadow-md hover:shadow-lg"
            >
              Создать аккаунт
            </button>
          </form>
          
          <p className="mt-6 text-center text-gray-600">
            Уже есть аккаунт?{' '}
            <Link to="/signin" className="text-[#3D2D4C] hover:text-[#2D1D3C] font-medium">
              Войти
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignUp;