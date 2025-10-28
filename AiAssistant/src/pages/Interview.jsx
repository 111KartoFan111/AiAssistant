import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import InterviewService from '../services/interviewService';

const Interview = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { interviewId, initialQuestion, position, questionType, currentQuestionNumber, totalQuestions } = location.state || {};
  
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState(currentQuestionNumber || 1);
  const [currentType, setCurrentType] = useState(questionType || 'BACKGROUND');
  const [isComplete, setIsComplete] = useState(false);
  const chatEndRef = useRef(null);

  useEffect(() => {
    if (initialQuestion) {
      setMessages([{ 
        role: 'AI', 
        content: initialQuestion, 
        timestamp: new Date(),
        questionType: questionType || 'BACKGROUND',
        questionNumber: 1
      }]);
    } else {
        navigate('/interview-setup');
    }
  }, [initialQuestion, navigate, questionType]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const getQuestionTypeLabel = (type) => {
    const types = {
      'BACKGROUND': { label: 'Background Question', color: 'bg-blue-100 text-blue-800', emoji: '👤' },
      'SITUATIONAL': { label: 'Situational Question', color: 'bg-purple-100 text-purple-800', emoji: '💡' },
      'TECHNICAL': { label: 'Technical Question', color: 'bg-green-100 text-green-800', emoji: '⚙️' }
    };
    return types[type] || types['BACKGROUND'];
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!userInput.trim() || isLoading || isComplete) return;

    const userMessage = { role: 'USER', content: userInput, timestamp: new Date() };
    setMessages(prev => [...prev, userMessage]);
    setUserInput('');
    setIsLoading(true);

    try {
      const response = await InterviewService.submitAnswer(interviewId, { answer: userInput });
      
      // Проверяем, завершено ли интервью
      if (response.data.isInterviewComplete) {
        const completionMessage = { 
          role: 'AI', 
          content: response.data.content,
          timestamp: new Date(),
          isCompletion: true
        };
        setMessages(prev => [...prev, completionMessage]);
        setIsComplete(true);
        
        // Автоматически завершаем интервью через 3 секунды
        setTimeout(() => {
          navigate('/progress');
        }, 3000);
      } else {
        const aiMessage = { 
          role: 'AI', 
          content: response.data.nextQuestion, 
          timestamp: new Date(),
          questionType: response.data.questionType,
          questionNumber: response.data.currentQuestionNumber
        };
        setMessages(prev => [...prev, aiMessage]);
        setCurrentQuestion(response.data.currentQuestionNumber);
        setCurrentType(response.data.questionType);
      }
    } catch (error) {
      console.error("Failed to get next question:", error);
      const errorMessage = { role: 'AI', content: "Sorry, I faced an error. Please try again.", timestamp: new Date() };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEndInterview = async () => {
    try {
      await InterviewService.completeInterview(interviewId);
    } catch (error) {
      console.error("Error completing interview:", error);
    }
    navigate('/progress');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex-1">
            <h2 className="text-xl font-semibold text-gray-800">{position || 'Interview'}</h2>
            
            {/* Progress Bar */}
            <div className="mt-2 flex items-center gap-3">
              <div className="flex-1 bg-gray-200 rounded-full h-2.5 overflow-hidden">
                <div 
                  className="bg-blue-500 h-2.5 rounded-full transition-all duration-500"
                  style={{ width: `${(currentQuestion / (totalQuestions || 20)) * 100}%` }}
                ></div>
              </div>
              <span className="text-sm font-medium text-gray-600 whitespace-nowrap">
                {currentQuestion} / {totalQuestions || 20}
              </span>
            </div>

            {/* Current Question Type Badge */}
            {!isComplete && (
              <div className="mt-2">
                <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium ${getQuestionTypeLabel(currentType).color}`}>
                  <span>{getQuestionTypeLabel(currentType).emoji}</span>
                  {getQuestionTypeLabel(currentType).label}
                </span>
              </div>
            )}
          </div>

          <button 
            onClick={handleEndInterview}
            className="bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-2 rounded-lg transition ml-4"
          >
            End Interview
          </button>
        </div>
      </header>

      <main className="flex-1 p-6 flex justify-center">
        <div className="w-full max-w-4xl flex flex-col">
          <div className="flex-1 space-y-6 overflow-y-auto p-4 bg-white rounded-t-xl shadow-inner">
            {messages.map((msg, index) => (
              <div key={index}>
                {/* Question Type Header for AI messages */}
                {msg.role === 'AI' && msg.questionType && !msg.isCompletion && (
                  <div className="flex items-center justify-center mb-2">
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <div className="h-px bg-gray-300 flex-1 w-12"></div>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getQuestionTypeLabel(msg.questionType).color}`}>
                        {getQuestionTypeLabel(msg.questionType).emoji} Question {msg.questionNumber}
                      </span>
                      <div className="h-px bg-gray-300 flex-1 w-12"></div>
                    </div>
                  </div>
                )}
                
                <div className={`flex ${msg.role === 'USER' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-lg p-4 rounded-2xl ${
                    msg.role === 'USER' 
                      ? 'bg-blue-500 text-white' 
                      : msg.isCompletion
                      ? 'bg-green-100 text-green-800 border-2 border-green-300'
                      : 'bg-gray-200 text-gray-800'
                  }`}>
                    {msg.isCompletion && (
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-2xl">🎉</span>
                        <span className="font-semibold">Interview Complete!</span>
                      </div>
                    )}
                    <p>{msg.content}</p>
                  </div>
                </div>
              </div>
            ))}
            
            {isLoading && (
              <div className="flex justify-start">
                 <div className="max-w-lg p-4 rounded-2xl bg-gray-200 text-gray-800">
                    <div className="flex items-center space-x-2">
                        <div className="w-2 h-2 bg-gray-500 rounded-full animate-pulse"></div>
                        <div className="w-2 h-2 bg-gray-500 rounded-full animate-pulse delay-75"></div>
                        <div className="w-2 h-2 bg-gray-500 rounded-full animate-pulse delay-150"></div>
                    </div>
                 </div>
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          <div className="bg-white p-4 border-t border-gray-200 rounded-b-xl">
            {isComplete ? (
              <div className="text-center py-4">
                <p className="text-gray-600 mb-2">Redirecting to progress page...</p>
                <div className="inline-flex items-center gap-2">
                  <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>
                  <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse delay-75"></div>
                  <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse delay-150"></div>
                </div>
              </div>
            ) : (
              <form onSubmit={handleSendMessage} className="flex gap-4">
                <input
                  type="text"
                  value={userInput}
                  onChange={(e) => setUserInput(e.target.value)}
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                  placeholder="Type your answer..."
                  disabled={isLoading}
                />
                <button
                  type="submit"
                  className="bg-blue-500 hover:bg-blue-600 text-white font-semibold px-6 py-3 rounded-lg transition disabled:bg-gray-400"
                  disabled={isLoading}
                >
                  Send
                </button>
              </form>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default Interview;