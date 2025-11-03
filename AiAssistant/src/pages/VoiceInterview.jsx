import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Header from '../components/Header';

const VoiceInterview = () => {
  const { interviewId } = useParams();
  const navigate = useNavigate();

  const [interviewData, setInterviewData] = useState(null);
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [isRecording, setIsRecording] = useState(false);
  const [isAIProcessing, setIsAIProcessing] = useState(false);
  const [conversation, setConversation] = useState([]);
  const [error, setError] = useState(null);
  const [audioLevel, setAudioLevel] = useState(0);
  const [isAISpeaking, setIsAISpeaking] = useState(false);

  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioContextRef = useRef(null);
  const analyserRef = useRef(null);
  const animationFrameRef = useRef(null);
  const audioElementRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    loadInterviewData();
    return () => {
      cleanup();
    };
  }, [interviewId]);

  const cleanup = () => {
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
    }
    if (audioContextRef.current) {
      audioContextRef.current.close();
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
    }
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      mediaRecorderRef.current.stop();
    }
  };

  const loadInterviewData = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/v1/voice-interviews/${interviewId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setInterviewData(response.data);
      setConversation(response.data.conversation || []);
      
      if (response.data.conversation && response.data.conversation.length > 0) {
        const lastMessage = response.data.conversation[response.data.conversation.length - 1];
        if (lastMessage.role === 'MODEL') {
          setCurrentQuestion(lastMessage);
        }
      }
    } catch (err) {
      console.error('Error loading interview:', err);
      setError('Не удалось загрузить интервью');
    }
  };

  const startRecording = async () => {
    try {
      console.log('Starting recording...');
      const stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          sampleRate: 44100
        } 
      });

      streamRef.current = stream;

      audioContextRef.current = new (window.AudioContext || window.webkitAudioContext)();
      analyserRef.current = audioContextRef.current.createAnalyser();
      const source = audioContextRef.current.createMediaStreamSource(stream);
      source.connect(analyserRef.current);
      analyserRef.current.fftSize = 256;

      visualizeAudioLevel();

      const options = {
        mimeType: 'audio/webm;codecs=opus'
      };

      if (!MediaRecorder.isTypeSupported(options.mimeType)) {
        options.mimeType = 'audio/webm';
      }
      

      mediaRecorderRef.current = new MediaRecorder(stream, options);
      audioChunksRef.current = [];

      mediaRecorderRef.current.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorderRef.current.onstop = async () => {
        console.log('Recording stopped, processing audio...');
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        
        if (streamRef.current) {
          streamRef.current.getTracks().forEach(track => track.stop());
          streamRef.current = null;
        }
        
        await sendAudioToBackend(audioBlob);
      };

      mediaRecorderRef.current.start();
      setIsRecording(true);
      setError(null);
      console.log('Recording started successfully');
    } catch (err) {
      console.error('Error starting recording:', err);
      setError('Не удалось получить доступ к микрофону. Проверьте разрешения.');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      console.log('Stopping MediaRecorder...');
      setTimeout(() => { // добавляем задержку 300мс
        mediaRecorderRef.current.stop();
      }, 300);
      setIsRecording(false);
      setAudioLevel(0);
    }
  };


  const visualizeAudioLevel = () => {
    if (!analyserRef.current) return;

    const bufferLength = analyserRef.current.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);

    const animate = () => {
      animationFrameRef.current = requestAnimationFrame(animate);
      analyserRef.current.getByteFrequencyData(dataArray);
      
      const average = dataArray.reduce((a, b) => a + b) / bufferLength;
      setAudioLevel(Math.min(100, (average / 255) * 100 * 3));
    };

    animate();
  };

  const sendAudioToBackend = async (audioBlob) => {
    setIsAIProcessing(true);
    
    try {
      const formData = new FormData();
      formData.append('audio', audioBlob, 'recording.webm');

      const token = localStorage.getItem('token');
      const response = await axios.post(
        `http://localhost:8080/api/v1/voice-interviews/${interviewId}/answer-audio`,
        formData,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      const userMessage = {
        role: 'USER',
        textContent: response.data.transcribedText,
        timestamp: Date.now()
      };
      setConversation(prev => [...prev, userMessage]);

      if (response.data.isInterviewComplete) {
        setTimeout(() => {
          navigate(`/interview-results/${interviewId}`);
        }, 2000);
      } else {
        const aiMessage = {
          role: 'MODEL',
          textContent: response.data.nextQuestionText,
          audioBase64: response.data.nextQuestionAudioBase64,
          questionType: response.data.questionType,
          timestamp: Date.now()
        };
        setConversation(prev => [...prev, aiMessage]);
        setCurrentQuestion(aiMessage);

        if (response.data.nextQuestionAudioBase64) {
          playAIAudio(response.data.nextQuestionAudioBase64);
        }
      }

    } catch (err) {
      console.error('Error sending audio:', err);
      setError('Ошибка при отправке аудио. Попробуйте ещё раз.');
    } finally {
      setIsAIProcessing(false);
    }
  };

  const playAIAudio = (base64Audio) => {
    try {
      setIsAISpeaking(true);
      
      const audioData = atob(base64Audio);
      const arrayBuffer = new ArrayBuffer(audioData.length);
      const view = new Uint8Array(arrayBuffer);
      for (let i = 0; i < audioData.length; i++) {
        view[i] = audioData.charCodeAt(i);
      }
      
      const blob = new Blob([arrayBuffer], { type: 'audio/mp3' });
      const audioUrl = URL.createObjectURL(blob);
      
      if (audioElementRef.current) {
        audioElementRef.current.pause();
      }
      
      audioElementRef.current = new Audio(audioUrl);
      audioElementRef.current.onended = () => {
        setIsAISpeaking(false);
        URL.revokeObjectURL(audioUrl);
      };
      
      audioElementRef.current.play();
    } catch (err) {
      console.error('Error playing audio:', err);
      setIsAISpeaking(false);
    }
  };

  const handleCompleteInterview = async () => {
    try {
      const token = localStorage.getItem('token');
      await axios.post(
        `http://localhost:8080/api/v1/voice-interviews/${interviewId}/complete`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      navigate(`/interview-results/${interviewId}`);
    } catch (err) {
      console.error('Error completing interview:', err);
      setError('Не удалось завершить интервью');
    }
  };

  const getQuestionTypeLabel = (type) => {
    const types = {
      'BACKGROUND': { label: 'Биография', color: 'bg-blue-100 text-blue-700' },
      'SITUATIONAL': { label: 'Ситуационный', color: 'bg-purple-100 text-purple-700' },
      'TECHNICAL': { label: 'Технический', color: 'bg-green-100 text-green-700' }
    };
    return types[type] || { label: type, color: 'bg-gray-100 text-gray-700' };
  };

  if (!interviewData) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="flex items-center justify-center h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#3D2D4C] mx-auto mb-4"></div>
            <p className="text-gray-600">Загрузка интервью...</p>
          </div>
        </div>
      </div>
    );
  }

  const progress = interviewData.conversation 
    ? (interviewData.conversation.filter(m => m.role === 'MODEL').length / 20) * 100 
    : 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="max-w-5xl mx-auto px-6 pt-6 pb-20">
        <div className="mb-6 flex items-center justify-between">
          <button 
            onClick={() => navigate('/dashboard')}
            className="flex items-center text-gray-600 hover:text-gray-900 transition"
          >
            <span className="mr-2">←</span> Выйти из интервью
          </button>
          
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm text-gray-600">Прогресс</p>
              <p className="text-lg font-bold text-[#3D2D4C]">
                {Math.floor(progress / 5)} / 20 вопросов
              </p>
            </div>
            <div className="w-32 bg-gray-200 rounded-full h-2">
              <div 
                className="bg-[#3D2D4C] h-2 rounded-full transition-all"
                style={{ width: `${progress}%` }}
              ></div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white rounded-2xl shadow-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-bold text-gray-900">
                  Текущий вопрос
                </h2>
                {currentQuestion && (
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getQuestionTypeLabel(currentQuestion.questionType).color}`}>
                    {getQuestionTypeLabel(currentQuestion.questionType).label}
                  </span>
                )}
              </div>

              {currentQuestion && (
                <div className="bg-gray-50 rounded-xl p-6 mb-6">
                  <p className="text-lg text-gray-800 leading-relaxed">
                    {currentQuestion.textContent}
                  </p>
                </div>
              )}

              <div className="flex flex-col items-center justify-center py-8">
                {isAISpeaking && (
                  <div className="mb-6 text-center">
                    <div className="inline-flex items-center gap-2 bg-blue-100 text-blue-700 px-4 py-2 rounded-full">
                      <div className="flex gap-1">
                        <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                        <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                        <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                      </div>
                      <span className="font-medium">AI отвечает...</span>
                    </div>
                  </div>
                )}

                <div className="flex gap-4 items-center mb-6">
                  <div className="relative">
                    <button
                      onClick={startRecording}
                      disabled={isRecording || isAIProcessing || isAISpeaking}
                      className={`w-32 h-32 rounded-full flex items-center justify-center transition-all shadow-xl ${
                        isRecording 
                          ? 'bg-red-500 animate-pulse' 
                          : 'bg-[#3D2D4C] hover:bg-[#2D1D3C]'
                      } disabled:bg-gray-300 disabled:cursor-not-allowed`}
                    >
                      <svg className="w-12 h-12 text-white" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                        <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
                      </svg>
                    </button>

                    {isRecording && (
                      <div 
                        className="absolute inset-0 rounded-full border-4 border-red-400 opacity-50"
                        style={{
                          transform: `scale(${1 + audioLevel / 50})`,
                          transition: 'transform 0.1s ease-out'
                        }}
                      ></div>
                    )}
                  </div>

                  {isRecording && (
                    <button
                      onClick={stopRecording}
                      className="px-8 py-4 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl transition shadow-lg flex items-center gap-2"
                    >
                      <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <rect x="6" y="6" width="12" height="12" rx="2"/>
                      </svg>
                      СТОП
                    </button>
                  )}
                </div>

                <p className="text-center text-gray-600 mb-2 text-lg font-medium">
                  {isRecording ? '🎙️ Говорите... (нажмите СТОП когда закончите)' : isAIProcessing ? '⏳ Обработка...' : isAISpeaking ? '🔊 Слушайте...' : '👆 Нажмите микрофон для ответа'}
                </p>
                
                {isRecording && (
                  <div className="w-64 h-3 bg-gray-200 rounded-full overflow-hidden mt-2">
                    <div 
                      className="h-full bg-red-500 transition-all duration-100"
                      style={{ width: `${audioLevel}%` }}
                    ></div>
                  </div>
                )}
              </div>

              {error && (
                <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-red-700 text-sm">{error}</p>
                </div>
              )}
            </div>

            <div className="flex gap-4">
              <button
                onClick={handleCompleteInterview}
                disabled={isRecording}
                className="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-3 rounded-lg transition disabled:bg-gray-400"
              >
                Завершить интервью
              </button>
            </div>
          </div>

          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-lg p-6 sticky top-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4">История беседы</h3>
              
              <div className="space-y-3 max-h-[600px] overflow-y-auto">
                {conversation.length === 0 ? (
                  <p className="text-gray-500 text-sm text-center py-8">
                    История пока пуста
                  </p>
                ) : (
                  conversation.map((message, index) => (
                    <div
                      key={index}
                      className={`p-3 rounded-lg ${
                        message.role === 'USER' 
                          ? 'bg-blue-50 ml-4' 
                          : 'bg-gray-50 mr-4'
                      }`}
                    >
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`font-semibold text-sm ${
                          message.role === 'USER' ? 'text-blue-700' : 'text-[#3D2D4C]'
                        }`}>
                          {message.role === 'USER' ? 'Вы' : 'AI'}
                        </span>
                        {message.questionType && (
                          <span className={`text-xs px-2 py-0.5 rounded-full ${getQuestionTypeLabel(message.questionType).color}`}>
                            {getQuestionTypeLabel(message.questionType).label}
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-gray-700">
                        {message.textContent}
                      </p>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VoiceInterview;