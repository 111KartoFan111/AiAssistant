import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isAuthed, setIsAuthed] = useState(() => Boolean(localStorage.getItem('token')));

  useEffect(() => {
    const updateAuth = () => setIsAuthed(Boolean(localStorage.getItem('token')));
    updateAuth();
    window.addEventListener('storage', updateAuth);
    return () => window.removeEventListener('storage', updateAuth);
  }, []);

  return (
    <header className="relative z-50">
      {/* Main header */}
      <nav className="bg-[#3D2D4C] sticky top-0">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div 
              onClick={() => navigate('/')}
              className="flex items-center gap-2 cursor-pointer group"
            >
              <div className="w-9 h-9 bg-white rounded-xl flex items-center justify-center shadow-lg group-hover:shadow-xl transition-all">
                <span className="text-[#3D2D4C] text-xl font-bold">AI</span>
              </div>
              <span className="text-xl font-bold text-white">
                InterviewAI
              </span>
            </div>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center gap-8">
              <a href="#features" className="text-white hover:text-gray-200 text-sm font-medium transition-colors">
                О продукте
              </a>
            </div>

            {/* Desktop Buttons */}
            <div className="hidden md:flex items-center gap-3">
              {isAuthed ? (
                <button 
                  onClick={() => navigate('/dashboard')}
                  className="bg-white text-[#3D2D4C] hover:bg-gray-100 font-semibold px-6 py-2.5 rounded-xl text-sm transition-all shadow-lg hover:shadow-xl"
                >
                  В кабинет
                </button>
              ) : (
                <>
                  <button 
                    onClick={() => navigate('/signin')}
                    className="text-white hover:text-gray-200 font-semibold px-5 py-2 text-sm transition-colors"
                  >
                    Войти
                  </button>
                  <button 
                    onClick={() => navigate('/signup')}
                    className="bg-white text-[#3D2D4C] hover:bg-gray-100 font-semibold px-6 py-2.5 rounded-xl text-sm transition-all shadow-lg hover:shadow-xl"
                  >
                    Регистрация
                  </button>
                </>
              )}
            </div>

            {/* Mobile menu button */}
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden p-2 hover:bg-[#4D3D5C] rounded-lg transition-colors"
            >
              <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                {isMenuOpen ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>

          {/* Mobile menu */}
          {isMenuOpen && (
            <div className="md:hidden py-4 border-t border-[#4D3D5C]">
              <div className="flex flex-col gap-1">
                <a href="#features" className="px-4 py-3 text-white hover:bg-[#4D3D5C] text-sm font-medium rounded-lg transition-colors">
                  О продукте
                </a>
                <div className="border-t border-[#4D3D5C] my-2"></div>
                {isAuthed ? (
                  <button 
                    onClick={() => navigate('/dashboard')}
                    className="mx-4 bg-white text-[#3D2D4C] font-semibold px-4 py-3 rounded-xl text-sm shadow-lg"
                  >
                    В кабинет
                  </button>
                ) : (
                  <>
                    <button 
                      onClick={() => navigate('/signin')}
                      className="mx-4 px-4 py-3 text-white hover:bg-[#4D3D5C] text-sm font-semibold text-left rounded-lg transition-colors"
                    >
                      Войти
                    </button>
                    <button 
                      onClick={() => navigate('/signup')}
                      className="mx-4 bg-white text-[#3D2D4C] font-semibold px-4 py-3 rounded-xl text-sm shadow-lg"
                    >
                      Регистрация
                    </button>
                  </>
                )}
              </div>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
};

export default Header;