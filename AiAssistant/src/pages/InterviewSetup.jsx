import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import InterviewService from '../services/interviewService';

const InterviewSetup = () => {
  const navigate = useNavigate();
  const [field, setField] = useState('it'); 
  const [position, setPosition] = useState('Senior Java Developer');
  const [jobDescription, setJobDescription] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleStartInterview = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await InterviewService.startInterview({ position, jobDescription });
      const { interviewId, firstQuestion } = response.data;

      navigate('/interview', {
        state: {
          interviewId,
          initialQuestion: firstQuestion,
          position
        }
      });
    } catch (err) {
      setError('Failed to start interview. Please try again.');
      console.error('Start interview error:', err);
      setIsLoading(false);
    }
  };

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
          <p className="text-gray-600 mb-8">Configure your interview simulation</p>
          
          <form onSubmit={handleStartInterview} className="space-y-6">
            {/* Сфера деятельности */}
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
              </select>
            </div>

            {/* Должность */}
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

            {/* Описание вакансии */}
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
            
            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-4 rounded-lg transition shadow-md hover:shadow-lg disabled:bg-gray-400"
            >
              {isLoading ? 'Starting...' : 'Start Interview'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default InterviewSetup;
