import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 relative overflow-hidden">
      {/* Декоративные круги на фоне */}
      <div className="absolute left-0 bottom-0 w-96 h-96 bg-orange-50 rounded-full blur-3xl opacity-60 -translate-x-1/2 translate-y-1/2"></div>
      <div className="absolute right-0 top-1/4 w-96 h-96 bg-blue-100 rounded-full blur-3xl opacity-40 translate-x-1/3"></div>
      
      {/* Контейнер */}
      <div className="relative z-10">
        <Header />
        
        <main className="max-w-4xl mx-auto px-6 pt-32 pb-20 text-center">
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6 leading-tight">
            Interview preparation<br />assistant
          </h1>
          
          <p className="text-xl text-gray-600 mb-12 max-w-2xl mx-auto">
            An AI assistant to help you prepare for your<br />next job interview
          </p>
          
          <button 
            onClick={() => navigate('/signup')}
            className="bg-blue-500 hover:bg-blue-600 text-white font-semibold px-12 py-4 rounded-xl text-lg transition shadow-lg hover:shadow-xl"
          >
            Get started
          </button>
        </main>
      </div>
    </div>
  );
};

export default LandingPage;