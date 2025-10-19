import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const Dashboard = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-7xl mx-auto px-6 pt-10 pb-20">
        <h1 className="text-4xl font-bold text-gray-900 mb-8">Dashboard</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Карточка 1 - New Interview */}
          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer"
               onClick={() => navigate('/interview-setup')}>
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
              <span className="text-2xl">📝</span>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">New Interview</h3>
            <p className="text-gray-600 mb-4">Start a new interview simulation</p>
            <button className="text-blue-500 hover:text-blue-600 font-medium">
              Get started →
            </button>
          </div>
          
          {/* Карточка 2 */}
         {/* Карточка 2 - My Progress */}
<div 
  className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer"
  onClick={() => navigate('/progress')}
>
  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
    <span className="text-2xl">📊</span>
  </div>
  <h3 className="text-xl font-semibold text-gray-900 mb-2">My Progress</h3>
  <p className="text-gray-600 mb-4">View your interview history</p>
  <button className="text-blue-500 hover:text-blue-600 font-medium">
    View details →
  </button>
</div>
          
          {/* Карточка 3 */}
         {/* Карточка 3 - My Resume */}
<div 
  className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer"
  onClick={() => navigate('/resume')}
>
  <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
    <span className="text-2xl">📄</span>
  </div>
  <h3 className="text-xl font-semibold text-gray-900 mb-2">My Resume</h3>
  <p className="text-gray-600 mb-4">Manage your resume</p>
  <button className="text-blue-500 hover:text-blue-600 font-medium">
    Edit resume →
  </button>
</div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;