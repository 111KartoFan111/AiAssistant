import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import InterviewService from '../services/interviewService';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';

const InterviewSetup = () => {
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
      const response = await InterviewService.startInterview({ 
        position, 
        jobDescription, 
        language,
        company 
      });
      const { interviewId, firstQuestion, questionType, currentQuestionNumber, totalQuestions } = response.data;

      navigate('/interview', {
        state: {
          interviewId,
          initialQuestion: firstQuestion,
          position,
          questionType,
          currentQuestionNumber,
          totalQuestions
        }
      });
    } catch (err) {
      setError('Failed to start interview. Please try again.');
      console.error('Start interview error:', err);
      setIsLoading(false);
    }
  };

  const companies = [
    { id: 'kaspi', name: 'Kaspi', logo: '🏦' },
    { id: 'jusan', name: 'Jusan', logo: '🏛️' },
    { id: 'halyk', name: 'Halyk Bank', logo: '🏦' },
    { id: 'kolesa', name: 'Kolesa Group', logo: '🚗' },
    { id: 'air-astana', name: 'Air Astana', logo: '✈️' },
    { id: 'kazmunaygas', name: 'KazMunayGas', logo: '⛽' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-3xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Back to Dashboard
        </button>

        <div className="bg-white rounded-2xl shadow-lg p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Setup Your Interview</h1>
          <p className="text-gray-600 mb-8">Configure your interview simulation (20 questions)</p>

          <form onSubmit={handleStartInterview} className="space-y-6">
            {/* Interview Language */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Interview Language *
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
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-blue-300'
                    }`}
                  >
                    <span className="text-xl mr-2">{lang.flag}</span>
                    <span className="font-medium">{lang.name}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Select Company */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Select Company (Optional)
              </label>
              <Swiper
                spaceBetween={20}
                slidesPerView={3}
                breakpoints={{
                  640: { slidesPerView: 2 },
                  768: { slidesPerView: 3 },
                }}
                className="company-swiper"
              >
                {companies.map((comp) => (
                  <SwiperSlide key={comp.id}>
                    <button
                      type="button"
                      onClick={() => setCompany(comp.id)}
                      className={`w-full p-4 rounded-lg border-2 transition-all ${
                        company === comp.id
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-blue-300'
                      }`}
                    >
                      <div className="text-2xl mb-2">{comp.logo}</div>
                      <div className="font-medium text-sm">{comp.name}</div>
                    </button>
                  </SwiperSlide>
                ))}
              </Swiper>
            </div>

            {/* Field / Industry */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Field / Industry *
              </label>
              <select
                value={field}
                onChange={(e) => setField(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                required
              >
                <option value="">Select a field</option>
                <option value="it">Information Technology</option>
                <option value="finance">Finance</option>
                <option value="marketing">Marketing</option>
                <option value="engineering">Engineering</option>
                <option value="healthcare">Healthcare</option>
              </select>
            </div>

            {/* Position */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Position *
              </label>
              <input
                type="text"
                value={position}
                onChange={(e) => setPosition(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                placeholder="e.g. Senior Java Developer, Product Manager"
                required
              />
            </div>

            {/* Job Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Description (Optional)
              </label>
              <textarea
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
                rows="4"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition resize-none"
                placeholder="Paste the job description here for more relevant questions..."
              />
            </div>

            {/* Interview Structure Info */}
            <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
              <div className="flex items-start">
                <span className="text-blue-500 text-xl mr-3">📋</span>
                <div>
                  <p className="text-blue-900 font-medium mb-1">Interview Structure (20 Questions)</p>
                  <ul className="text-blue-700 text-sm space-y-1">
                    <li>• <strong>Background Questions (1-5):</strong> About your experience and career</li>
                    <li>• <strong>Situational Questions (6-13):</strong> How you handle various scenarios</li>
                    <li>• <strong>Technical Questions (14-20):</strong> Skills and domain knowledge</li>
                  </ul>
                </div>
              </div>
            </div>
            
            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-4 rounded-lg transition shadow-md hover:shadow-lg disabled:bg-gray-400"
            >
              {isLoading ? 'Starting...' : 'Start Interview (20 Questions)'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default InterviewSetup;