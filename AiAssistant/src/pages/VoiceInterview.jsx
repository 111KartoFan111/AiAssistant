import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { voiceInterviewService } from '../services/voiceInterviewService';
import { Mic, Square } from 'lucide-react';
import './VoiceInterview.css';

function VoiceInterview() {
    const { id } = useParams();
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

    useEffect(() => {
        // ✅ ИСПРАВЛЕНИЕ: Проверяем что id существует и валиден
        if (id && id !== 'undefined') {
            loadInterview();
        } else {
            setError('Неверный ID интервью');
        }
    }, [id]); // Добавляем id в зависимости

    const loadInterview = async () => {
        try {
            setError(null);
            const data = await voiceInterviewService.getInterviewDetails(id);
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
            
            const response = await voiceInterviewService.submitAudioAnswer(id, audioBlob);
            console.log('Response received:', response);
            
            if (response.success && response.aiResponse) {
                await loadInterview();
                speakText(response.aiResponse);
            }
        } catch (error) {
            console.error('Error submitting audio:', error);
            alert('Ошибка при отправке аудио: ' + error.message);
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

    // ✅ ИСПРАВЛЕНИЕ: Показываем ошибку если есть
    if (error) {
        return (
            <div className="voice-interview-container">
                <div className="error-message">
                    <p>{error}</p>
                    <button onClick={() => navigate('/voice-interview-setup')}>
                        Вернуться назад
                    </button>
                </div>
            </div>
        );
    }

    if (!interview) {
        return (
            <div className="voice-interview-container">
                <div className="loading">Загрузка...</div>
            </div>
        );
    }

    return (
        <div className="voice-interview-container">
            <div className="interview-header">
                <h2>{interview.positionTitle}</h2>
                <p className="company-name">{interview.companyName}</p>
            </div>

            <div className="messages-container">
                {messages.length === 0 ? (
                    <div className="no-messages">
                        <p>Нажмите на микрофон чтобы начать интервью</p>
                    </div>
                ) : (
                    messages.map((message, index) => (
                        <div key={index} className={`message ${message.sender}`}>
                            <div className="message-content">
                                {message.textContent || "🎤 Аудио сообщение"}
                            </div>
                        </div>
                    ))
                )}
            </div>

            <div className="interview-controls">
                {aiSpeaking && (
                    <div className="ai-speaking-indicator">
                        AI is speaking...
                    </div>
                )}
                
                {!isRecording ? (
                    <button 
                        className="record-button" 
                        onClick={startRecording}
                        disabled={isProcessing || aiSpeaking}
                    >
                        <Mic size={32} />
                        <span>Нажмите микрофон для ответа</span>
                    </button>
                ) : (
                    <button 
                        className="record-button recording" 
                        onClick={stopRecording}
                    >
                        <Square size={32} />
                        <span>Остановить запись</span>
                    </button>
                )}
                
                {isProcessing && (
                    <div className="processing-indicator">
                        Обработка...
                    </div>
                )}
            </div>
        </div>
    );
}

export default VoiceInterview;