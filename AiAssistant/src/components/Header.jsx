import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();

  return (
    <header className="bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        {/* Логотип */}
        <Link to="/" className="flex items-center gap-3 hover:opacity-80 transition">
          <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
            <span className="text-white text-2xl font-bold">?</span>
          </div>
          <div className="flex flex-col leading-tight">
            <span className="text-gray-800 font-semibold text-lg">interview</span>
            <span className="text-blue-500 font-semibold text-lg -mt-1">helper</span>
          </div>
        </Link>
        
        {/* Кнопки навигации */}
        <div className="flex items-center gap-6">
          <button 
            onClick={() => navigate('/signin')}
            className="text-gray-700 font-medium hover:text-gray-900 transition"
          >
            Sign in
          </button>
          <button 
            onClick={() => navigate('/signup')}
            className="text-gray-700 font-medium hover:text-gray-900 transition"
          >
            Sign up
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;