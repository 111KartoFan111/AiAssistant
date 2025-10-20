import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import InterviewService from '../services/interviewService';

const Progress = () => {
  const navigate = useNavigate();
  
  const [interviews, setInterviews] = useState([]);
  const [selectedInterview, setSelectedInterview] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await InterviewService.getInterviewHistory();
        setInterviews(response.data);
      } catch (err) {
        setError('Failed to load interview history.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchHistory();
  }, []);

  const handleSelectInterview = async (interview) => {
    try {
        // Сначала отображаем базовую информацию
        setSelectedInterview(interview);
        // Затем подгружаем детали (разговор)
        const response = await InterviewService.getInterviewDetails(interview.id);
        setSelectedInterview(response.data);
    } catch (err) {
        setError('Failed to load interview details.');
        console.error(err);
    }
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-7xl mx-auto px-6 pt-10 pb-20">
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Back to Dashboard
        </button>

        <h1 className="text-4xl font-bold text-gray-900 mb-2">My Progress</h1>
        <p className="text-gray-600 mb-8">Track your interview performance and improvements</p>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Список интервью */}
          <div className="lg:col-span-1 space-y-4">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Interview History</h2>
            {interviews.length > 0 ? interviews.map((interview) => (
              <div
                key={interview.id}
                onClick={() => handleSelectInterview(interview)}
                className={`bg-white rounded-xl shadow-md p-5 cursor-pointer transition hover:shadow-lg ${
                  selectedInterview?.id === interview.id ? 'ring-2 ring-blue-500' : ''
                }`}
              >
                <h3 className="font-semibold text-gray-900 mb-1">{interview.position}</h3>
                <div className="flex items-center gap-4 text-sm text-gray-600">
                  <span>📅 {new Date(interview.startTime).toLocaleDateString()}</span>
                  <span>{interview.status}</span>
                </div>
              </div>
            )) : <p>No interviews found.</p>}
          </div>

          {/* Детальный анализ */}
          <div className="lg:col-span-2">
            {selectedInterview ? (
              <div className="bg-white rounded-xl shadow-lg p-8">
                <h2 className="text-3xl font-bold text-gray-900 mb-2">{selectedInterview.position}</h2>
                <p className="text-gray-600">{new Date(selectedInterview.startTime).toLocaleString()}</p>
                <div className="mt-6 border-t pt-6">
                    <h3 className="text-xl font-bold text-gray-900 mb-4">Conversation</h3>
                    <div className="space-y-4 max-h-96 overflow-y-auto">
                        {selectedInterview.conversation?.map((msg, index) => (
                            <div key={index} className={`flex ${msg.role === 'USER' ? 'justify-end' : 'justify-start'}`}>
                                <div className={`max-w-lg p-3 rounded-xl ${
                                    msg.role === 'USER' 
                                    ? 'bg-blue-500 text-white' 
                                    : 'bg-gray-200 text-gray-800'
                                }`}>
                                    <p className="text-sm">{msg.content}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-xl shadow-lg p-12 text-center">
                <div className="text-6xl mb-4">📊</div>
                <h3 className="text-2xl font-bold text-gray-900 mb-2">Select an Interview</h3>
                <p className="text-gray-600">Click on any interview from the list to see details.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Progress;
