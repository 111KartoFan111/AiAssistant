import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Header from '../components/Header';

const InterviewResults = () => {
  const { interviewId } = useParams();
  const navigate = useNavigate();
  const [results, setResults] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isExporting, setIsExporting] = useState(false);

  useEffect(() => {
    loadResults();
  }, [interviewId]);

  const loadResults = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/v1/interviews/${interviewId}/evaluation`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setResults(response.data);
    } catch (err) {
      console.error('Error loading results:', err);
      setError('Не удалось загрузить результаты');
    } finally {
      setIsLoading(false);
    }
  };

  const handleExportPDF = async () => {
    setIsExporting(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/v1/export/${interviewId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
          responseType: 'blob'
        }
      );

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
      alert('Ошибка при экспорте PDF');
    } finally {
      setIsExporting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="flex items-center justify-center h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#3D2D4C] mx-auto mb-4"></div>
            <p className="text-gray-600">Загрузка результатов...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !results) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-6 pt-20">
          <div className="bg-white rounded-2xl shadow-lg p-8 text-center">
            <p className="text-red-600 mb-4">{error || 'Результаты не найдены'}</p>
            <button
              onClick={() => navigate('/dashboard')}
              className="bg-[#3D2D4C] text-white px-6 py-2 rounded-lg"
            >
              Вернуться на Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreBg = (score) => {
    if (score >= 80) return 'bg-green-50 border-green-200';
    if (score >= 60) return 'bg-yellow-50 border-yellow-200';
    return 'bg-red-50 border-red-200';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-5xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Вернуться на Dashboard
        </button>

        <div className="bg-white rounded-2xl shadow-lg p-8 mb-6">
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
              <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Интервью завершено!</h1>
            <p className="text-gray-600">Вот ваши результаты и обратная связь</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className={`p-6 rounded-xl border-2 ${getScoreBg(results.overallScore)}`}>
              <p className="text-sm text-gray-600 mb-1">Общий балл</p>
              <p className={`text-4xl font-bold ${getScoreColor(results.overallScore)}`}>
                {results.overallScore}/100
              </p>
            </div>

            <div className={`p-6 rounded-xl border-2 ${getScoreBg(results.communicationScore)}`}>
              <p className="text-sm text-gray-600 mb-1">Коммуникация</p>
              <p className={`text-4xl font-bold ${getScoreColor(results.communicationScore)}`}>
                {results.communicationScore}/100
              </p>
            </div>

            <div className={`p-6 rounded-xl border-2 ${getScoreBg(results.technicalScore)}`}>
              <p className="text-sm text-gray-600 mb-1">Технические навыки</p>
              <p className={`text-4xl font-bold ${getScoreColor(results.technicalScore)}`}>
                {results.technicalScore}/100
              </p>
            </div>
          </div>

          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-3">📊 Детальная оценка</h2>
              <div className="bg-gray-50 rounded-xl p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {results.detailedScores && Object.entries(results.detailedScores).map(([key, value]) => (
                    <div key={key} className="flex justify-between items-center">
                      <span className="text-gray-700 capitalize">
                        {key.replace(/([A-Z])/g, ' $1').trim()}:
                      </span>
                      <span className={`font-bold ${getScoreColor(value)}`}>{value}/100</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-3">💪 Сильные стороны</h2>
              <div className="bg-green-50 rounded-xl p-6">
                <ul className="space-y-2">
                  {results.strengths && results.strengths.map((strength, index) => (
                    <li key={index} className="flex items-start">
                      <span className="text-green-600 mr-2">✓</span>
                      <span className="text-gray-700">{strength}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-3">🎯 Области для улучшения</h2>
              <div className="bg-yellow-50 rounded-xl p-6">
                <ul className="space-y-2">
                  {results.improvements && results.improvements.map((improvement, index) => (
                    <li key={index} className="flex items-start">
                      <span className="text-yellow-600 mr-2">→</span>
                      <span className="text-gray-700">{improvement}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {results.feedback && (
              <div>
                <h2 className="text-xl font-bold text-gray-900 mb-3">💬 Общая обратная связь</h2>
                <div className="bg-blue-50 rounded-xl p-6">
                  <p className="text-gray-700 leading-relaxed">{results.feedback}</p>
                </div>
              </div>
            )}

            {results.recommendations && results.recommendations.length > 0 && (
              <div>
                <h2 className="text-xl font-bold text-gray-900 mb-3">📚 Рекомендации</h2>
                <div className="bg-purple-50 rounded-xl p-6">
                  <ul className="space-y-2">
                    {results.recommendations.map((recommendation, index) => (
                      <li key={index} className="flex items-start">
                        <span className="text-purple-600 mr-2">•</span>
                        <span className="text-gray-700">{recommendation}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>

          <div className="flex gap-4 mt-8">
            <button
              onClick={handleExportPDF}
              disabled={isExporting}
              className="flex-1 bg-[#3D2D4C] hover:bg-[#2D1D3C] text-white font-semibold py-3 rounded-lg transition disabled:bg-gray-400 flex items-center justify-center gap-2"
            >
              {isExporting ? (
                'Экспорт...'
              ) : (
                <>
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                  </svg>
                  Скачать PDF отчёт
                </>
              )}
            </button>
            <button
              onClick={() => navigate('/dashboard')}
              className="flex-1 bg-white hover:bg-gray-50 border-2 border-gray-300 text-gray-700 font-semibold py-3 rounded-lg transition"
            >
              Новое интервью
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InterviewResults;