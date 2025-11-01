import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const LandingPage = () => {
  const navigate = useNavigate();

  const features = [
    { 
      number: '1',
      title: 'Текстовые интервью',
      description: 'Практикуйтесь в любое время с AI-ассистентом в текстовом формате. Получайте мгновенную обратную связь на каждый ответ.'
    },
    { 
      number: '2',
      title: 'Голосовые интервью',
      description: 'Реалистичная симуляция собеседований с голосовым AI. Тренируйте речь и уверенность в реальном времени.'
    },
    { 
      number: '3',
      title: 'Под любую компанию',
      description: 'Выбирайте компанию и должность — AI адаптирует вопросы под специфику интервью в Google, Meta, Яндекс и других.'
    },
    { 
      number: '4',
      title: 'Детальная аналитика',
      description: 'Отслеживайте прогресс, анализируйте слабые места и получайте персональные рекомендации для улучшения.'
    },
  ];

  const companies = [
    { name: 'Kaspi Bank', logo: '/images/kaspi.png', hoverColor: 'hover:bg-red-500' },
    { name: 'Sulpak', logo: '/images/sulpak.png', hoverColor: 'hover:bg-orange-500' },
    { name: 'Народный банк', logo: '/images/halyk.png', hoverColor: 'hover:bg-green-500' },
    { name: 'Magnum', logo: '/images/magnum.png', hoverColor: 'hover:bg-pink-500' },
    { name: 'BI Group', logo: '/images/bi.png', hoverColor: 'hover:bg-blue-500' },
    { name: 'MEGA Alma-Ata', logo: '/images/mega.png', hoverColor: 'hover:bg-yellow-500' },
    { name: 'Kcell', logo: '/images/kcell.png', hoverColor: 'hover:bg-purple-500' },
    { name: 'Казахтелеком', logo: '/images/kazaq.svg', hoverColor: 'hover:bg-cyan-500' },
    { name: 'Банк ЦентрКредит', logo: '/images/credit.png', hoverColor: 'hover:bg-indigo-500' },
    { name: 'BAZIS-A', logo: '/images/bazis.png', hoverColor: 'hover:bg-gray-700' },
  ];

  return (
    <div className="min-h-screen bg-white">
      <Header />
      
      <main>
        {/* Hero Section */}
        <section className="relative overflow-hidden bg-gray-50 px-4 sm:px-6 py-20 sm:py-32">
          <div className="relative max-w-6xl mx-auto text-center">
            <h1 className="text-5xl sm:text-6xl md:text-7xl font-bold text-gray-900 mb-6 leading-tight">
              Мы научили ИИ проводить
              <br />
              <span className="text-gray-800">собеседования за вас</span>
            </h1>
            
            <p className="text-xl sm:text-2xl text-gray-700 mb-12 leading-relaxed max-w-3xl mx-auto">
              Тренируйтесь за реальные навыки, а не за слова в резюме
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-16">
              <button 
                onClick={() => navigate('/signup')}
                className="bg-[#3D2D4C] hover:bg-[#2D1D3C] text-white font-semibold px-10 py-4 rounded-xl text-lg transition-all shadow-lg hover:shadow-xl w-full sm:w-auto"
              >
                Начать бесплатно
              </button>
              
              <button 
                className="text-[#3D2D4C] font-semibold px-10 py-4 rounded-xl text-lg hover:bg-gray-100 transition-all w-full sm:w-auto border-2 border-[#3D2D4C]"
              >
                Хочу персональное демо
              </button>
            </div>
          </div>
        </section>

        {/* Demo Section */}
        <section className="bg-white py-16 sm:py-24">
          <div className="max-w-6xl mx-auto px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                Как это работает
              </h2>
              <p className="text-lg text-gray-600">
                Три простых шага к успешному собеседованию
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
              <div className="text-center">
                <div className="w-16 h-16 bg-[#3D2D4C] rounded-2xl flex items-center justify-center mx-auto mb-4 text-white text-2xl font-bold shadow-lg">
                  1
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Выберите компанию
                </h3>
                <p className="text-gray-600">
                  Укажите компанию и позицию, на которую идете
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-[#3D2D4C] rounded-2xl flex items-center justify-center mx-auto mb-4 text-white text-2xl font-bold shadow-lg">
                  2
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Практикуйтесь с AI
                </h3>
                <p className="text-gray-600">
                  Проходите интервью в текстовом или голосовом формате
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-[#3D2D4C] rounded-2xl flex items-center justify-center mx-auto mb-4 text-white text-2xl font-bold shadow-lg">
                  3
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Получайте фидбек
                </h3>
                <p className="text-gray-600">
                  Анализируйте ошибки и улучшайте свои навыки
                </p>
              </div>
            </div>

            {/* Interview Demo Box */}
            <div className="bg-gray-50 rounded-3xl shadow-xl border border-gray-200 p-8 sm:p-12 max-w-4xl mx-auto">
              <div className="flex items-center gap-3 mb-6 pb-6 border-b border-gray-200">
                <div className="w-12 h-12 bg-[#3D2D4C] rounded-full flex items-center justify-center">
                  <span className="text-2xl text-white font-bold">AI</span>
                </div>
                <div>
                  <div className="font-semibold text-gray-900 text-lg">AI Interviewer</div>
                  <div className="text-sm text-green-600 flex items-center gap-1">
                    <span className="w-2 h-2 bg-green-600 rounded-full animate-pulse"></span>
                    Готов к интервью
                  </div>
                </div>
              </div>

              <div className="space-y-4">
                <div className="bg-white border border-gray-200 p-5 rounded-2xl shadow-sm">
                  <p className="text-gray-800 mb-3 font-medium">
                    "Расскажите о ситуации, когда вам пришлось работать под давлением для соблюдения жесткого дедлайна."
                  </p>
                  <div className="flex gap-2 mt-3 flex-wrap">
                    <span className="text-xs bg-gray-100 text-gray-700 px-3 py-1 rounded-full font-medium">Поведенческий</span>
                    <span className="text-xs bg-gray-100 text-gray-700 px-3 py-1 rounded-full font-medium">STAR метод</span>
                    <span className="text-xs bg-gray-100 text-gray-700 px-3 py-1 rounded-full font-medium">Google Style</span>
                  </div>
                </div>

                <div className="bg-gray-50 border border-gray-200 p-5 rounded-2xl ml-8">
                  <p className="text-gray-700 mb-3">
                    "В моей предыдущей компании у нас была презентация для клиента через 48 часов, а наш дизайнер заболел..."
                  </p>
                  <div className="flex gap-3 text-sm text-gray-600 flex-wrap">
                    <span className="flex items-center gap-1">
                      <span className="text-green-600 font-bold">✓</span> Хорошая структура
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="text-green-600 font-bold">✓</span> Четкий timeline
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="text-yellow-600 font-bold">!</span> Добавьте результат
                    </span>
                  </div>
                </div>

                <div className="flex gap-3 pt-2">
                  <button className="flex-1 bg-[#3D2D4C] text-white py-4 rounded-xl font-semibold hover:bg-[#2D1D3C] transition-all shadow-lg">
                    Продолжить практику
                  </button>
                  <button className="px-6 bg-white border-2 border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 transition-colors font-medium">
                    ⏸
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="bg-gray-50 py-16 sm:py-24">
          <div className="max-w-7xl mx-auto px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-3">
                Наши преимущества
              </h2>
              <p className="text-xl text-[#3D9DD4]">
                выгодно / качественно / доступно
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {/* Card 1 - Practice More */}
              <div className="bg-white p-8 rounded-2xl shadow-md hover:shadow-lg transition-shadow text-center">
                <div className="flex items-center justify-center mb-6">
                  <svg className="w-20 h-20 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">
                  Практика 24/7
                </h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  AI доступен в любое время для тренировок. Проходите интервью когда удобно вам - утром, днем или ночью.
                </p>
              </div>

              {/* Card 2 - AI Feedback */}
              <div className="bg-white p-8 rounded-2xl shadow-md hover:shadow-lg transition-shadow text-center">
                <div className="flex items-center justify-center mb-6">
                  <svg className="w-20 h-20 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M9 2C6.24 2 4 4.24 4 7v10c0 2.76 2.24 5 5 5h9c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2h-3V5c0-1.66-1.34-3-3-3H9zm0 2h3c.55 0 1 .45 1 1v2H9c-1.66 0-3 1.34-3 3v8c0 1.66 1.34 3 3 3h9V9h-9c-1.1 0-2-.9-2-2V7c0-1.66 1.34-3 3-3z"/>
                    <path d="M12 15c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/>
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">
                  Мгновенный фидбек
                </h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  Получайте детальный анализ каждого ответа сразу после интервью с конкретными рекомендациями для улучшения.
                </p>
              </div>

              {/* Card 3 - Company-Specific */}
              <div className="bg-white p-8 rounded-2xl shadow-md hover:shadow-lg transition-shadow text-center">
                <div className="flex items-center justify-center mb-6">
                  <svg className="w-20 h-20 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/>
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">
                  Под любую компанию
                </h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  Выбирайте компанию мечты - AI адаптирует вопросы под специфику и культуру каждой организации.
                </p>
              </div>

              {/* Card 4 - Progress Tracking */}
              <div className="bg-white p-8 rounded-2xl shadow-md hover:shadow-lg transition-shadow text-center">
                <div className="flex items-center justify-center mb-6">
                  <svg className="w-20 h-20 text-[#3D2D4C]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">
                  Отслеживание прогресса
                </h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  Следите за своим развитием с помощью детальной аналитики и визуализации улучшений по всем навыкам.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Platform Features Section */}
        <section className="bg-white py-16 sm:py-24">
          <div className="max-w-6xl mx-auto px-4 sm:px-6">
            <div className="text-center mb-16">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                Всё необходимое для подготовки
              </h2>
              <p className="text-lg text-gray-600 max-w-2xl mx-auto">
                Наша платформа предоставляет все инструменты для эффективной подготовки к собеседованию
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {features.map((feature, index) => (
                <div key={index} className="bg-gray-50 p-8 rounded-2xl hover:shadow-lg transition-all border border-gray-100">
                  <div className="w-14 h-14 bg-[#3D2D4C] rounded-xl flex items-center justify-center mb-4">
                    <span className="text-3xl text-white font-bold">{feature.number}</span>
                  </div>
                  <h3 className="text-xl font-bold text-gray-900 mb-3">
                    {feature.title}
                  </h3>
                  <p className="text-gray-600 leading-relaxed">
                    {feature.description}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Companies Section */}
        <section className="bg-white py-16 sm:py-24">
          <div className="max-w-6xl mx-auto px-4 sm:px-6">
            <div className="text-center mb-12">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                Готовьтесь к интервью в топовых компаниях
              </h2>
              <p className="text-lg text-gray-600">
                AI адаптирует вопросы под стиль и культуру каждой компании
              </p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
              {companies.map((company, index) => (
                <div 
                  key={index} 
                  className={`group bg-white p-6 rounded-2xl transition-all duration-300 cursor-pointer border-2 border-gray-200 hover:border-transparent ${company.hoverColor} hover:shadow-xl hover:scale-105 flex flex-col items-center justify-center min-h-[140px]`}
                >
                  <div className="w-full h-16 mb-3 flex items-center justify-center">
                    <img 
                      src={company.logo} 
                      alt={company.name}
                      className="max-w-full max-h-full object-contain transition-all duration-300 group-hover:brightness-0 group-hover:invert"
                    />
                  </div>
                  <span className="text-sm font-semibold text-gray-700 text-center group-hover:text-white transition-colors duration-300">
                    {company.name}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Stats Section */}
        <section className="bg-white py-16 sm:py-24">
          <div className="max-w-6xl mx-auto px-4 sm:px-6">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-12 text-center">
              <div>
                <div className="text-6xl font-bold text-[#3D2D4C] mb-2">
                  5K+
                </div>
                <p className="text-gray-600 text-lg">Довольных пользователей</p>
              </div>
              <div>
                <div className="text-6xl font-bold text-[#3D2D4C] mb-2">
                  92%
                </div>
                <p className="text-gray-600 text-lg">Процент успеха</p>
              </div>
              <div>
                <div className="text-6xl font-bold text-[#3D2D4C] mb-2">
                  25K+
                </div>
                <p className="text-gray-600 text-lg">Проведенных интервью</p>
              </div>
            </div>
          </div>
        </section>

        {/* Final CTA */}
        <section className="relative overflow-hidden bg-[#3D2D4C] py-20 sm:py-28">
          <div className="relative max-w-4xl mx-auto px-4 sm:px-6 text-center">
            <h2 className="text-4xl sm:text-5xl font-bold text-white mb-6">
              Готовы получить оффер мечты?
            </h2>
            <p className="text-xl text-gray-200 mb-10 max-w-2xl mx-auto">
              Присоединяйтесь к тысячам кандидатов, которые успешно подготовились и получили желаемую работу
            </p>
            <button 
              onClick={() => navigate('/signup')}
              className="bg-white text-[#3D2D4C] font-bold px-12 py-5 rounded-xl text-lg hover:bg-gray-100 transition-all shadow-2xl hover:shadow-xl inline-block"
            >
              Начать бесплатно
            </button>
            <p className="text-gray-200 text-sm mt-4">
              Без кредитной карты • 7 дней бесплатно
            </p>
          </div>
        </section>
      </main>
    </div>
  );
};

export default LandingPage;