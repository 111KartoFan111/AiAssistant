import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import ProgressService from '../services/progressService';

const Progress = () => {
  const navigate = useNavigate();
  
  const [analytics, setAnalytics] = useState(null);
  const [selectedInterviewId, setSelectedInterviewId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const response = await ProgressService.getProgressAnalytics();
      setAnalytics(response.data);
    } catch (err) {
      setError('Не удалось загрузить данные прогресса');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExportPDF = async (interviewId) => {
    setIsExporting(true);
    try {
      const response = await ProgressService.exportInterviewPDF(interviewId);
      
      // Создаем blob и скачиваем файл
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `interview-results-${interviewId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error exporting PDF:', err);
      setError('Не удалось экспортировать PDF');
    } finally {
      setIsExporting(false);
    }
  };

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreBgColor = (score) => {
    if (score >= 80) return 'bg-green-100';
    if (score >= 60) return 'bg-yellow-100';
    return 'bg-red-100';
  };

  const getTrendIcon = (trend) => {
    if (trend === 'Improving') return '📈';
    if (trend === 'Declining') return '📉';
    return '➡️';
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-6 pt-20 text-center">
          <p className="text-gray-600">Загрузка данных прогресса...</p>
        </div>
      </div>
    );
  }

  if (error && !analytics) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-6 pt-20 text-center">
          <p className="text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-7xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Назад к Dashboard
        </button>

        <h1 className="text-4xl font-bold text-gray-900 mb-2">Мой прогресс</h1>
        <p className="text-gray-600 mb-8">Отслеживайте свою производительность и улучшения</p>

        {error && (
          <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 rounded">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">Всего интервью</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">
                  {analytics?.totalInterviews || 0}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <svg className="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">Средний балл</p>
                <p className={`text-3xl font-bold mt-2 ${getScoreColor(analytics?.averageScore || 0)}`}>
                  {analytics?.averageScore?.toFixed(1) || '0'}
                </p>
              </div>
              <div className={`w-12 h-12 rounded-full flex items-center justify-center ${getScoreBgColor(analytics?.averageScore || 0)}`}>
                <svg className={`w-6 h-6 ${getScoreColor(analytics?.averageScore || 0)}`} fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">Улучшение</p>
                <p className={`text-3xl font-bold mt-2 ${analytics?.scoreImprovement >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {analytics?.scoreImprovement >= 0 ? '+' : ''}{analytics?.scoreImprovement?.toFixed(1) || '0'}%
                </p>
              </div>
              <div className={`w-12 h-12 rounded-full flex items-center justify-center ${analytics?.scoreImprovement >= 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                <svg className={`w-6 h-6 ${analytics?.scoreImprovement >= 0 ? 'text-green-600' : 'text-red-600'}`} fill="currentColor" viewBox="0 0 24 24">
                  {analytics?.scoreImprovement >= 0 ? (
                    <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
                  ) : (
                    <path d="M16 18l2.29-2.29-4.88-4.88-4 4L2 7.41 3.41 6l6 6 4-4 6.3 6.29L22 12v6z"/>
                  )}
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Skills Progress */}
        {analytics?.skillsProgress && Object.keys(analytics.skillsProgress).length > 0 && (
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Прогресс по навыкам</h2>
            <div className="space-y-4">
              {Object.entries(analytics.skillsProgress).map(([key, skill]) => (
                <div key={key} className="border-b pb-4 last:border-b-0">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-gray-900">{skill.skillName}</span>
                      <span className="text-xl">{getTrendIcon(skill.trend)}</span>
                    </div>
                    <div className="text-right">
                      <span className={`text-lg font-bold ${getScoreColor(skill.currentScore)}`}>
                        {skill.currentScore?.toFixed(1)}
                      </span>
                      {skill.improvement !== 0 && (
                        <span className={`ml-2 text-sm ${skill.improvement > 0 ? 'text-green-600' : 'text-red-600'}`}>
                          ({skill.improvement > 0 ? '+' : ''}{skill.improvement?.toFixed(1)}%)
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className={`h-2 rounded-full ${skill.currentScore >= 80 ? 'bg-green-500' : skill.currentScore >= 60 ? 'bg-yellow-500' : 'bg-red-500'}`}
                      style={{ width: `${skill.currentScore}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Recent Interviews */}
        {analytics?.recentInterviews && analytics.recentInterviews.length > 0 && (
          <div className="bg-white rounded-xl shadow-md p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Последние интервью</h2>
            <div className="space-y-3">
              {analytics.recentInterviews.map((interview) => (
                <div
                  key={interview.interviewId}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition cursor-pointer"
                  onClick={() => setSelectedInterviewId(interview.interviewId)}
                >
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{interview.position}</h3>
                    <div className="flex items-center gap-4 text-sm text-gray-600 mt-1">
                      <span>📅 {new Date(interview.date).toLocaleDateString('ru-RU')}</span>
                      <span className={`px-2 py-1 rounded ${
                        interview.status === 'COMPLETED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                      }`}>
                        {interview.status === 'COMPLETED' ? 'Завершено' : interview.status}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className={`text-2xl font-bold ${getScoreColor(interview.score)}`}>
                      {interview.score?.toFixed(1) || 'N/A'}
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleExportPDF(interview.interviewId);
                      }}
                      disabled={isExporting}
                      className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition disabled:bg-gray-400 flex items-center gap-2"
                    >
                      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2z"/>
                      </svg>
                      {isExporting ? 'Экспорт...' : 'PDF'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Empty State */}
        {analytics?.totalInterviews === 0 && (
          <div className="bg-white rounded-xl shadow-md p-12 text-center">
            <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-12 h-12 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
                <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Пока нет интервью</h3>
            <p className="text-gray-600 mb-6">
              Начните свое первое интервью, чтобы увидеть прогресс здесь
            </p>
            <button
              onClick={() => navigate('/interview-setup')}
              className="px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-medium transition"
            >
              Начать интервью
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Progress;