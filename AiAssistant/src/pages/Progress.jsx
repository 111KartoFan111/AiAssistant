import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

const Progress = () => {
  const navigate = useNavigate();
  
  // История интервью (потом будет из API)
  const [interviews, setInterviews] = useState([
    {
      id: 1,
      date: '2024-10-10',
      position: 'Senior Java Developer',
      field: 'Information Technology',
      duration: '45 min',
      questionsTotal: 10,
      questionsAnswered: 10,
      overallScore: 85,
      mode: 'voice',
      analysis: {
        strengths: [
          'Excellent technical knowledge of Java and Spring Boot',
          'Clear communication and structured answers',
          'Good examples from past experience',
          'Confident presentation of problem-solving skills'
        ],
        improvements: [
          'Could provide more specific metrics in achievements',
          'Sometimes answers were too brief',
          'Need to work on behavioral question responses'
        ],
        speechAnalysis: {
          wordsPerMinute: 145,
          fillerWords: 12,
          pauseTime: '15%',
          clarity: 88,
          confidence: 82
        },
        categoryScores: {
          technical: 90,
          behavioral: 78,
          communication: 85,
          problemSolving: 88
        }
      }
    },
    {
      id: 2,
      date: '2024-10-05',
      position: 'Product Manager',
      field: 'Marketing',
      duration: '38 min',
      questionsTotal: 8,
      questionsAnswered: 8,
      overallScore: 78,
      mode: 'text',
      analysis: {
        strengths: [
          'Strong understanding of product lifecycle',
          'Good stakeholder management examples',
          'Well-structured responses'
        ],
        improvements: [
          'Need more concrete metrics and data',
          'Could elaborate more on leadership experience',
          'Add more real-world examples'
        ],
        speechAnalysis: null,
        categoryScores: {
          technical: 75,
          behavioral: 82,
          communication: 76,
          problemSolving: 79
        }
      }
    },
    {
      id: 3,
      date: '2024-09-28',
      position: 'Frontend Developer',
      field: 'Information Technology',
      duration: '42 min',
      questionsTotal: 9,
      questionsAnswered: 9,
      overallScore: 72,
      mode: 'voice',
      analysis: {
        strengths: [
          'Good knowledge of React and modern frameworks',
          'Showed enthusiasm and passion',
          'Creative problem-solving approach'
        ],
        improvements: [
          'Need to work on explaining complex concepts simply',
          'Reduce filler words (um, uh)',
          'Provide more structured answers'
        ],
        speechAnalysis: {
          wordsPerMinute: 160,
          fillerWords: 28,
          pauseTime: '22%',
          clarity: 75,
          confidence: 70
        },
        categoryScores: {
          technical: 80,
          behavioral: 68,
          communication: 70,
          problemSolving: 75
        }
      }
    }
  ]);

  const [selectedInterview, setSelectedInterview] = useState(null);

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600 bg-green-100';
    if (score >= 60) return 'text-yellow-600 bg-yellow-100';
    return 'text-red-600 bg-red-100';
  };

  const getScoreLabel = (score) => {
    if (score >= 80) return 'Excellent';
    if (score >= 60) return 'Good';
    return 'Needs Improvement';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-7xl mx-auto px-6 pt-10 pb-20">
        {/* Навигация */}
        <button 
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition"
        >
          <span className="mr-2">←</span> Back to Dashboard
        </button>

        <h1 className="text-4xl font-bold text-gray-900 mb-2">My Progress</h1>
        <p className="text-gray-600 mb-8">Track your interview performance and improvements</p>

        {/* Общая статистика */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-xl shadow-md p-6">
            <p className="text-gray-600 text-sm mb-1">Total Interviews</p>
            <p className="text-3xl font-bold text-gray-900">{interviews.length}</p>
          </div>
          <div className="bg-white rounded-xl shadow-md p-6">
            <p className="text-gray-600 text-sm mb-1">Average Score</p>
            <p className="text-3xl font-bold text-blue-600">
              {Math.round(interviews.reduce((acc, i) => acc + i.overallScore, 0) / interviews.length)}%
            </p>
          </div>
          <div className="bg-white rounded-xl shadow-md p-6">
            <p className="text-gray-600 text-sm mb-1">Total Time</p>
            <p className="text-3xl font-bold text-green-600">
              {interviews.reduce((acc, i) => acc + parseInt(i.duration), 0)} min
            </p>
          </div>
          <div className="bg-white rounded-xl shadow-md p-6">
            <p className="text-gray-600 text-sm mb-1">Questions Answered</p>
            <p className="text-3xl font-bold text-purple-600">
              {interviews.reduce((acc, i) => acc + i.questionsAnswered, 0)}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Список интервью */}
          <div className="lg:col-span-1 space-y-4">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Interview History</h2>
            {interviews.map((interview) => (
              <div
                key={interview.id}
                onClick={() => setSelectedInterview(interview)}
                className={`bg-white rounded-xl shadow-md p-5 cursor-pointer transition hover:shadow-lg ${
                  selectedInterview?.id === interview.id ? 'ring-2 ring-blue-500' : ''
                }`}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900 mb-1">
                      {interview.position}
                    </h3>
                    <p className="text-sm text-gray-600">{interview.field}</p>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getScoreColor(interview.overallScore)}`}>
                    {interview.overallScore}%
                  </span>
                </div>
                <div className="flex items-center gap-4 text-sm text-gray-600">
                  <span>📅 {interview.date}</span>
                  <span>⏱️ {interview.duration}</span>
                  <span>{interview.mode === 'voice' ? '🎤' : '💬'}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Детальный анализ */}
          <div className="lg:col-span-2">
            {selectedInterview ? (
              <div className="bg-white rounded-xl shadow-lg p-8">
                <div className="flex items-start justify-between mb-6">
                  <div>
                    <h2 className="text-3xl font-bold text-gray-900 mb-2">
                      {selectedInterview.position}
                    </h2>
                    <p className="text-gray-600">{selectedInterview.field}</p>
                    <p className="text-sm text-gray-500 mt-1">
                      {selectedInterview.date} • {selectedInterview.duration} • {selectedInterview.questionsAnswered} questions
                    </p>
                  </div>
                  <div className="text-center">
                    <div className={`text-5xl font-bold mb-1 ${getScoreColor(selectedInterview.overallScore).split(' ')[0]}`}>
                      {selectedInterview.overallScore}%
                    </div>
                    <span className="text-sm text-gray-600">
                      {getScoreLabel(selectedInterview.overallScore)}
                    </span>
                  </div>
                </div>

                {/* Оценки по категориям */}
                <div className="mb-8">
                  <h3 className="text-xl font-bold text-gray-900 mb-4">Performance by Category</h3>
                  <div className="space-y-4">
                    {Object.entries(selectedInterview.analysis.categoryScores).map(([category, score]) => (
                      <div key={category}>
                        <div className="flex justify-between mb-2">
                          <span className="font-medium text-gray-700 capitalize">
                            {category.replace(/([A-Z])/g, ' $1').trim()}
                          </span>
                          <span className="font-semibold text-gray-900">{score}%</span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-3">
                          <div
                            className={`h-3 rounded-full ${
                              score >= 80 ? 'bg-green-500' : score >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                            }`}
                            style={{ width: `${score}%` }}
                          ></div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Анализ речи (если голосовой режим) */}
                {selectedInterview.analysis.speechAnalysis && (
                  <div className="mb-8">
                    <h3 className="text-xl font-bold text-gray-900 mb-4">Speech Analysis</h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="bg-blue-50 rounded-lg p-4">
                        <p className="text-sm text-gray-600 mb-1">Speaking Speed</p>
                        <p className="text-2xl font-bold text-blue-600">
                          {selectedInterview.analysis.speechAnalysis.wordsPerMinute} WPM
                        </p>
                      </div>
                      <div className="bg-green-50 rounded-lg p-4">
                        <p className="text-sm text-gray-600 mb-1">Clarity</p>
                        <p className="text-2xl font-bold text-green-600">
                          {selectedInterview.analysis.speechAnalysis.clarity}%
                        </p>
                      </div>
                      <div className="bg-purple-50 rounded-lg p-4">
                        <p className="text-sm text-gray-600 mb-1">Confidence</p>
                        <p className="text-2xl font-bold text-purple-600">
                          {selectedInterview.analysis.speechAnalysis.confidence}%
                        </p>
                      </div>
                      <div className="bg-orange-50 rounded-lg p-4">
                        <p className="text-sm text-gray-600 mb-1">Filler Words</p>
                        <p className="text-2xl font-bold text-orange-600">
                          {selectedInterview.analysis.speechAnalysis.fillerWords}
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                {/* Сильные стороны */}
                <div className="mb-8">
                  <h3 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
                    <span className="text-green-500 mr-2">✓</span>
                    What You Did Well
                  </h3>
                  <ul className="space-y-3">
                    {selectedInterview.analysis.strengths.map((strength, index) => (
                      <li key={index} className="flex items-start bg-green-50 rounded-lg p-4">
                        <span className="text-green-500 mr-3 mt-0.5">✓</span>
                        <span className="text-gray-700">{strength}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                {/* Области для улучшения */}
                <div>
                  <h3 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
                    <span className="text-blue-500 mr-2">→</span>
                    Areas for Improvement
                  </h3>
                  <ul className="space-y-3">
                    {selectedInterview.analysis.improvements.map((improvement, index) => (
                      <li key={index} className="flex items-start bg-blue-50 rounded-lg p-4">
                        <span className="text-blue-500 mr-3 mt-0.5">→</span>
                        <span className="text-gray-700">{improvement}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                {/* Кнопка для нового интервью */}
                <div className="mt-8 pt-6 border-t border-gray-200">
                  <button
                    onClick={() => navigate('/interview-setup')}
                    className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-4 rounded-lg transition shadow-md hover:shadow-lg"
                  >
                    Practice Again with Similar Questions
                  </button>
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-xl shadow-lg p-12 text-center">
                <div className="text-6xl mb-4">📊</div>
                <h3 className="text-2xl font-bold text-gray-900 mb-2">
                  Select an Interview
                </h3>
                <p className="text-gray-600">
                  Click on any interview from the list to see detailed analysis
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Progress;