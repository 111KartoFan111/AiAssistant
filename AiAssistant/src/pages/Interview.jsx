import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const Interview = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { field, position, mode } = location.state || {};

  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [isRecording, setIsRecording] = useState(false);
  const [showKeyboard, setShowKeyboard] = useState(false);

  // Примерные вопросы (потом будут от AI)
  const questions = [
    {
      type: 'Background question',
      text: 'Can you please tell me a bit about yourself?'
    },
    {
      type: 'Technical question',
      text: 'What are your strongest technical skills?'
    },
    {
      type: 'Experience question',
      text: 'Tell me about a challenging project you worked on.'
    },
    {
      type: 'Behavioral question',
      text: 'How do you handle conflicts in a team?'
    },
    {
      type: 'Closing question',
      text: 'Do you have any questions for us?'
    }
  ];

  const currentQ = questions[currentQuestion];
  const totalQuestions = questions.length;

  const handleAnswer = () => {
    setIsRecording(!isRecording);
  };

  const handleNext = () => {
    if (currentQuestion < totalQuestions - 1) {
      setCurrentQuestion(currentQuestion + 1);
      setIsRecording(false);
    } else {
      // Завершение интервью
      navigate('/interview-results');
    }
  };

  const handleBack = () => {
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Хедер */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <button 
            onClick={handleBack}
            className="flex items-center text-gray-600 hover:text-gray-900 transition"
          >
            <span className="mr-2">←</span> Data Analytics
          </button>
          
          <div className="flex items-center gap-2">
            <span className="text-lg font-semibold">interview</span>
            <span className="text-blue-500 text-lg font-semibold">warmup</span>
          </div>
          
          <div className="flex items-center gap-4">
            <button className="text-gray-600 hover:text-gray-900">
              🔊
            </button>
            <button className="text-gray-600 hover:text-gray-900">
              ⋮
            </button>
          </div>
        </div>
      </header>

      {/* Основной контент */}
      <main className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-4xl w-full bg-white rounded-3xl shadow-xl p-8 md:p-12">
          {/* Тип вопроса и номер */}
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2 text-blue-600">
              <span className="text-lg">ℹ️</span>
              <span className="font-medium">{currentQ.type}</span>
            </div>
            <span className="text-gray-500 font-medium">
              {currentQuestion + 1}/{totalQuestions}
            </span>
          </div>

          {/* Вопрос */}
          <h2 className="text-3xl md:text-4xl font-semibold text-gray-800 mb-12">
            {currentQ.text}
          </h2>

          {/* Кнопки управления */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              {/* Кнопка Answer */}
              <button
                onClick={handleAnswer}
                className={`flex items-center gap-2 px-8 py-4 rounded-2xl font-semibold text-lg transition shadow-lg ${
                  isRecording
                    ? 'bg-red-500 hover:bg-red-600 text-white'
                    : 'bg-blue-500 hover:bg-blue-600 text-white'
                }`}
              >
                {isRecording ? (
                  <>
                    <span className="w-3 h-3 bg-white rounded-full animate-pulse"></span>
                    Recording...
                  </>
                ) : (
                  <>
                    <span className="text-xl">🎤</span>
                    Answer
                  </>
                )}
              </button>

              {/* Кнопка Keyboard */}
              <button
                onClick={() => setShowKeyboard(!showKeyboard)}
                className="p-4 border-2 border-gray-300 rounded-2xl hover:border-blue-400 hover:bg-blue-50 transition"
                title="Type answer"
              >
                <span className="text-xl">⌨️</span>
              </button>

              {/* Кнопка Hint */}
              <button
                className="p-4 border-2 border-gray-300 rounded-2xl hover:border-blue-400 hover:bg-blue-50 transition"
                title="Get hint"
              >
                <span className="text-xl">💡</span>
              </button>
            </div>

            {/* Кнопка Next */}
            <button
              onClick={handleNext}
              className="p-4 border-2 border-gray-300 rounded-2xl hover:border-blue-500 hover:bg-blue-50 transition"
            >
              <span className="text-xl text-blue-500">→</span>
            </button>
          </div>

          {/* Текстовое поле (если активен режим клавиатуры) */}
          {showKeyboard && (
            <div className="mt-6">
              <textarea
                className="w-full px-4 py-3 border-2 border-gray-300 rounded-xl focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition resize-none"
                rows="4"
                placeholder="Type your answer here..."
              />
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default Interview;