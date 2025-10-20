import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import InterviewService from '../services/interviewService';

const Interview = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { interviewId, initialQuestion, position } = location.state || {};
  
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const chatEndRef = useRef(null);

  // Инициализация чата при первой загрузке
  useEffect(() => {
    if (initialQuestion) {
      setMessages([{ role: 'AI', content: initialQuestion, timestamp: new Date() }]);
    } else {
        // Если данных нет, возвращаем пользователя назад
        navigate('/interview-setup');
    }
  }, [initialQuestion, navigate]);

  // Прокрутка к последнему сообщению
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!userInput.trim() || isLoading) return;

    const userMessage = { role: 'USER', content: userInput, timestamp: new Date() };
    setMessages(prev => [...prev, userMessage]);
    setUserInput('');
    setIsLoading(true);

    try {
      const response = await InterviewService.submitAnswer(interviewId, { answer: userInput });
      const aiMessage = { role: 'AI', content: response.data.nextQuestion, timestamp: new Date() };
      setMessages(prev => [...prev, aiMessage]);
    } catch (error) {
      console.error("Failed to get next question:", error);
      const errorMessage = { role: 'AI', content: "Sorry, I faced an error. Please try again.", timestamp: new Date() };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEndInterview = () => {
    // TODO: Добавить вызов API для завершения интервью и получения фидбэка
    navigate('/progress');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-800">{position || 'Interview'}</h2>
          <button 
            onClick={handleEndInterview}
            className="bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-2 rounded-lg transition"
          >
            End Interview
          </button>
        </div>
      </header>

      <main className="flex-1 p-6 flex justify-center">
        <div className="w-full max-w-4xl flex flex-col">
          {/* Область чата */}
          <div className="flex-1 space-y-6 overflow-y-auto p-4 bg-white rounded-t-xl shadow-inner">
            {messages.map((msg, index) => (
              <div key={index} className={`flex ${msg.role === 'USER' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-lg p-4 rounded-2xl ${
                  msg.role === 'USER' 
                    ? 'bg-blue-500 text-white' 
                    : 'bg-gray-200 text-gray-800'
                }`}>
                  <p>{msg.content}</p>
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

          {/* Поле ввода */}
          <div className="bg-white p-4 border-t border-gray-200 rounded-b-xl">
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
          </div>
        </div>
      </main>
    </div>
  );
};

export default Interview;