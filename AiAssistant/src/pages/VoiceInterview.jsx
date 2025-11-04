import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { voiceInterviewService } from '../services/voiceInterviewService';
import { Mic, Square } from 'lucide-react';

function VoiceInterview() {
    const { interviewId } = useParams();
    const navigate = useNavigate();
    
    const [interview, setInterview] = useState(null);
    const [messages, setMessages] = useState([]);
    const [isRecording, setIsRecording] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [aiSpeaking, setAiSpeaking] = useState(false);
    const [error, setError] = useState(null);
    
    const mediaRecorderRef = useRef(null);
    const audioChunksRef = useRef([]);
    const audioContextRef = useRef(null);
    const audioElementRef = useRef(null);
    const chatEndRef = useRef(null);

    useEffect(() => {
        // ✅ Проверяем, что параметр маршрута существует и валиден
        if (interviewId && interviewId !== 'undefined') {
            loadInterview();
        } else {
            setError('Неверный ID интервью');
        }
    }, [interviewId]);

    const loadInterview = async () => {
        try {
            setError(null);
            const data = await voiceInterviewService.getInterviewDetails(interviewId);
            setInterview(data.interview);
            setMessages(data.messages || []);
        } catch (error) {
            console.error('Failed to load interview:', error);
            setError('Не удалось загрузить интервью');
        }
    };

    const startRecording = async () => {
        try {
            console.log('Starting recording...');
            
            const stream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    channelCount: 1,
                    sampleRate: 24000,
                    echoCancellation: true,
                    noiseSuppression: true
                } 
            });
            
            const mimeType = 'audio/webm;codecs=opus';
            
            mediaRecorderRef.current = new MediaRecorder(stream, {
                mimeType: mimeType,
                audioBitsPerSecond: 128000
            });
            
            audioChunksRef.current = [];
            
            mediaRecorderRef.current.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    console.log('Audio chunk received:', event.data.size, 'bytes');
                    audioChunksRef.current.push(event.data);
                }
            };
            
            mediaRecorderRef.current.onstop = async () => {
                console.log('Recording stopped, processing audio...');
                console.log('Total chunks:', audioChunksRef.current.length);
                
                const audioBlob = new Blob(audioChunksRef.current, { type: mimeType });
                console.log('Audio blob size:', audioBlob.size, 'bytes');
                
                if (audioBlob.size > 0) {
                    await submitAudio(audioBlob);
                } else {
                    console.error('Audio blob is empty!');
                    alert('Ошибка: не удалось записать аудио');
                }
                
                stream.getTracks().forEach(track => track.stop());
            };
            
            mediaRecorderRef.current.start(100);
            setIsRecording(true);
            console.log('Recording started successfully');
            
        } catch (error) {
            console.error('Error starting recording:', error);
            alert('Ошибка доступа к микрофону: ' + error.message);
        }
    };

    const stopRecording = () => {
        if (mediaRecorderRef.current && isRecording) {
            console.log('Stopping MediaRecorder...');
            mediaRecorderRef.current.stop();
            setIsRecording(false);
        }
    };

    const submitAudio = async (audioBlob) => {
        try {
            setIsProcessing(true);
            console.log('Submitting audio, size:', audioBlob.size);
            
            const response = await voiceInterviewService.submitAudioAnswer(interviewId, audioBlob);
            console.log('Response received:', response);
            
            if (response.success && response.aiResponse) {
                await loadInterview();
                speakText(response.aiResponse);
            }
        } catch (error) {
            console.error('Error submitting audio:', error);
            const serverMsg = error?.response?.data?.message || error.message;
            alert('Ошибка при отправке аудио: ' + serverMsg);
        } finally {
            setIsProcessing(false);
        }
    };

    const speakText = (text) => {
        if ('speechSynthesis' in window) {
            setAiSpeaking(true);
            
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'ru-RU';
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            
            utterance.onend = () => {
                setAiSpeaking(false);
            };
            
            utterance.onerror = (error) => {
                console.error('Speech synthesis error:', error);
                setAiSpeaking(false);
            };
            
            window.speechSynthesis.speak(utterance);
        } else {
            console.error('Speech synthesis not supported');
        }
    };

    useEffect(() => {
        chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Ошибка
    if (error) {
        return (
            <div className="min-h-screen bg-gray-50">
                <div className="max-w-3xl mx-auto px-6 pt-10">
                    <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-6">
                        <p className="font-medium mb-3">{error}</p>
                        <button
                            onClick={() => navigate('/voice-interview-setup')}
                            className="bg-red-600 hover:bg-red-700 text-white font-semibold px-4 py-2 rounded-lg"
                        >
                            Вернуться назад
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!interview) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-gray-600">Загрузка...</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            {/* Sticky page header */}
            <header className="bg-white shadow-sm sticky top-0 z-10">
                <div className="max-w-4xl mx-auto px-6 py-4">
                    <h2 className="text-xl font-semibold text-gray-900">{interview.positionTitle}</h2>
                    {interview.companyName && (
                        <p className="text-sm text-gray-600 mt-1">{interview.companyName}</p>
                    )}
                    <div className="mt-2 flex items-center gap-2">
                        {aiSpeaking && (
                            <span className="inline-flex items-center gap-2 text-sm text-green-700 bg-green-100 border border-green-200 rounded-full px-3 py-1">
                                <span className="w-2 h-2 bg-green-600 rounded-full animate-pulse"></span>
                                AI говорит
                            </span>
                        )}
                        {isProcessing && (
                            <span className="inline-flex items-center gap-2 text-sm text-blue-700 bg-blue-100 border border-blue-200 rounded-full px-3 py-1">
                                <span className="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></span>
                                Обработка...
                            </span>
                        )}
                    </div>
                </div>
            </header>

            {/* Content */}
            <main className="flex-1 p-6 flex justify-center">
                <div className="w-full max-w-4xl flex flex-col">
                    {/* Messages */}
                    <div className="flex-1 space-y-6 overflow-y-auto p-4 bg-white rounded-t-xl shadow-inner">
                        {messages.length === 0 ? (
                            <div className="text-center text-gray-600 py-6">
                                Нажмите на микрофон чтобы начать интервью
                            </div>
                        ) : (
                            messages.map((message, index) => (
                                <div key={index} className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
                                    <div className={`max-w-lg p-4 rounded-2xl ${
                                        message.sender === 'user'
                                            ? 'bg-blue-500 text-white'
                                            : 'bg-gray-200 text-gray-800'
                                    }`}>
                                        {message.textContent || '🎤 Аудио сообщение'}
                                    </div>
                                </div>
                            ))
                        )}
                        <div ref={chatEndRef} />
                    </div>

                    {/* Controls */}
                    <div className="bg-white p-4 border-t border-gray-200 rounded-b-xl flex items-center justify-between">
                        <button
                            onClick={async () => { try { await voiceInterviewService.completeInterview(interviewId); } finally { navigate('/progress'); } }}
                            className="text-red-600 hover:text-red-700 font-semibold px-4 py-2 rounded-lg border border-red-200 hover:bg-red-50"
                        >
                            Завершить интервью
                        </button>

                        {!isRecording ? (
                            <button
                                className="inline-flex items-center gap-3 bg-blue-500 hover:bg-blue-600 text-white font-semibold px-5 py-3 rounded-xl transition disabled:bg-gray-400"
                                onClick={startRecording}
                                disabled={isProcessing || aiSpeaking}
                            >
                                <Mic size={22} />
                                <span>Нажмите микрофон для ответа</span>
                            </button>
                        ) : (
                            <button
                                className="inline-flex items-center gap-3 bg-red-500 hover:bg-red-600 text-white font-semibold px-5 py-3 rounded-xl transition"
                                onClick={stopRecording}
                            >
                                <Square size={22} />
                                <span>Остановить запись</span>
                            </button>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
}

export default VoiceInterview;