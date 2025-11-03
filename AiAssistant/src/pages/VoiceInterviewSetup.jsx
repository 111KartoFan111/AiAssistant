import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Header from '../components/Header';

const VoiceInterviewSetup = () => {
  const navigate = useNavigate();
  const [field, setField] = useState('it'); 
  const [position, setPosition] = useState('Senior Java Developer');
  const [jobDescription, setJobDescription] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [language, setLanguage] = useState('en');
  const [company, setCompany] = useState('');

  const handleStartInterview = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        'http://localhost:8080/api/v1/voice-interviews/start',
        { 
          position, 
          jobDescription, 
          language,
          company 
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      const { interviewId } = response.data;
      navigate(`/voice-interview/${interviewId}`);
    } catch (err) {
      setError('Не удалось начать голосовое интервью. Попробуйте ещё раз.');
      console.error('Start voice interview error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const companies = [
    { 
      id: 'kaspi', 
      name: 'Kaspi Bank', 
      logo: '/images/kaspi.png',
      hoverColor: 'hover:border-red-500',
      hoverBg: 'hover:bg-red-50'
    },
    { 
      id: 'sulpak', 
      name: 'Sulpak', 
      logo: '/images/sulpak.png',
      hoverColor: 'hover:border-orange-500',
      hoverBg: 'hover:bg-orange-50'
    },
    { 
      id: 'halyk', 
      name: 'Народный банк', 
      logo: '/images/halyk.png',
      hoverColor: 'hover:border-green-500',
      hoverBg: 'hover:bg-green-50'
    },
    { 
      id: 'magnum', 
      name: 'Magnum', 
      logo: '/images/magnum.png',
      hoverColor: 'hover:border-pink-500',
      hoverBg: 'hover:bg-pink-50'
    },
    { 
      id: 'bi', 
      name: 'BI Group', 
      logo: '/images/bi.png',
      hoverColor: 'hover:border-blue-500',
      hoverBg: 'hover:bg-blue-50'
    },
    { 
      id: 'mega', 
      name: 'MEGA Alma-Ata', 
      logo: '/images/mega.png',
      hoverColor: 'hover:border-yellow-500',
      hoverBg: 'hover:bg-yellow-50'
    },
    { 
      id: 'kcell', 
      name: 'Kcell', 
      logo: '/images/kcell.png',
      hoverColor: 'hover:border-purple-500',
      hoverBg: 'hover:bg-purple-50'
    },
    { 
      id: 'kazaq', 
      name: 'Казахтелеком', 
      logo: '/images/kazaq.svg',
      hoverColor: 'hover:border-cyan-500',
      hoverBg: 'hover:bg-cyan-50'
    },
    { 
      id: 'credit', 
      name: 'Банк ЦентрКредит', 
      logo: '/images/credit.png',
      hoverColor: 'hover:border-indigo-500',
      hoverBg: 'hover:bg-indigo-50'
    },
    { 
      id: 'bazis', 
      name: 'BAZIS-A', 
      logo: '/images/bazis.png',
      hoverColor: 'hover:border-gray-600',
      hoverBg: 'hover:bg-gray-50'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-3xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Назад к Dashboard
        </button>

        <div className="bg-white rounded-2xl shadow-lg p-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 bg-[#3D2D4C] rounded-lg flex items-center justify-center">
              <svg className="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Настройка голосового интервью</h1>
          </div>
          <p className="text-gray-600 mb-8">Настройте параметры голосового интервью (20 вопросов)</p>

          <form onSubmit={handleStartInterview} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Язык интервью *
              </label>
              <div className="grid grid-cols-3 gap-4">
                {[
                  { id: 'en', name: 'English', flag: '🇬🇧' },
                  { id: 'ru', name: 'Русский', flag: '🇷🇺' },
                  { id: 'kz', name: 'Қазақша', flag: '🇰🇿' }
                ].map((lang) => (
                  <button
                    key={lang.id}
                    type="button"
                    onClick={() => setLanguage(lang.id)}
                    className={`flex items-center justify-center p-4 rounded-lg border-2 transition-all ${
                      language === lang.id
                        ? 'border-[#3D2D4C] bg-[#3D2D4C] bg-opacity-10'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <span className="text-2xl mr-2">{lang.flag}</span>
                    <span className="font-medium text-gray-900">{lang.name}</span>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Компания (Опционально)
              </label>
              <div className="grid grid-cols-3 sm:grid-cols-5 gap-3">
                {companies.map((comp) => (
                  <button
                    key={comp.id}
                    type="button"
                    onClick={() => setCompany(comp.id === company ? '' : comp.id)}
                    className={`group p-3 rounded-xl border-2 transition-all ${
                      company === comp.id
                        ? 'border-[#3D2D4C] bg-[#3D2D4C] bg-opacity-5'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    title={comp.name}
                  >
                    <img 
                      src={comp.logo} 
                      alt={comp.name}
                      className="w-full h-12 object-contain"
                    />
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Сфера деятельности *
              </label>
              <select
                value={field}
                onChange={(e) => setField(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
              >
                <option value="it">IT и Технологии</option>
                <option value="finance">Финансы</option>
                <option value="marketing">Маркетинг</option>
                <option value="engineering">Инженерия</option>
                <option value="healthcare">Здравоохранение</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Должность *
              </label>
              <input
                type="text"
                value={position}
                onChange={(e) => setPosition(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition"
                placeholder="Например: Senior Java Developer, Product Manager"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Описание вакансии (Опционально)
              </label>
              <textarea
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
                rows="4"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#3D2D4C] focus:border-transparent outline-none transition resize-none"
                placeholder="Вставьте описание вакансии для более релевантных вопросов..."
              />
            </div>

            <div className="bg-[#3D2D4C] bg-opacity-10 border-l-4 border-[#3D2D4C] p-4 rounded">
              <div className="flex items-start">
                <span className="text-[#3D2D4C] text-xl mr-3">🎙️</span>
                <div>
                  <p className="text-gray-900 font-medium mb-1">Структура голосового интервью (20 вопросов)</p>
                  <ul className="text-gray-700 text-sm space-y-1">
                    <li>• <strong>Вопросы о биографии (1-5):</strong> О вашем опыте и карьере</li>
                    <li>• <strong>Ситуационные вопросы (6-13):</strong> Как вы справляетесь с различными сценариями</li>
                    <li>• <strong>Технические вопросы (14-20):</strong> Навыки и знание предметной области</li>
                    <li className="mt-2 text-[#3D2D4C] font-semibold">💡 Говорите четко и уверенно в микрофон</li>
                  </ul>
                </div>
              </div>
            </div>
            
            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-[#3D2D4C] hover:bg-[#2D1D3C] text-white font-semibold py-4 rounded-lg transition shadow-md hover:shadow-lg disabled:bg-gray-400 flex items-center justify-center gap-2"
            >
              {isLoading ? (
                'Запуск...'
              ) : (
                <>
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                    <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
                  </svg>
                  Начать голосовое интервью (20 вопросов)
                </>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default VoiceInterviewSetup;