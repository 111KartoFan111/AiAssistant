package com.zharkyn.aiassistant_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с OpenAI Realtime API
 * Использует WebSocket для real-time аудио коммуникации
 */
@Slf4j
@Service
public class OpenAIRealtimeService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String REALTIME_API_URL = "wss://api.openai.com/v1/realtime?model=gpt-realtime-2025-08-25";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Инициализировать Realtime сессию
     */
    public RealtimeSession initializeSession(String position, String jobDescription, 
                                             String language, int questionNumber) {
        try {
            log.info("Initializing OpenAI Realtime session");
            
            StandardWebSocketClient client = new StandardWebSocketClient();
            RealtimeWebSocketHandler handler = new RealtimeWebSocketHandler();
            
            // Подключаемся к WebSocket
            WebSocketSession session = client.execute(handler, 
                getWebSocketHeaders(), 
                java.net.URI.create(REALTIME_API_URL)).get();
            
            // Настраиваем сессию
            configureSession(session, position, jobDescription, language, questionNumber);
            
            return new RealtimeSession(session, handler);
            
        } catch (Exception e) {
            log.error("Error initializing realtime session", e);
            throw new RuntimeException("Failed to initialize realtime session: " + e.getMessage(), e);
        }
    }

    /**
     * Отправить аудио данные в сессию
     */
    public void sendAudio(WebSocketSession session, byte[] audioData) throws IOException {
        String base64Audio = Base64.getEncoder().encodeToString(audioData);
        
        Map<String, Object> audioMessage = Map.of(
            "type", "input_audio_buffer.append",
            "audio", base64Audio
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(audioMessage)));
        log.debug("Sent audio chunk, size: {} bytes", audioData.length);
    }

    /**
     * Завершить ввод аудио и получить транскрипцию
     */
    public void commitAudio(WebSocketSession session) throws IOException {
        Map<String, Object> commitMessage = Map.of(
            "type", "input_audio_buffer.commit"
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(commitMessage)));
        log.info("Committed audio buffer");
    }

    /**
     * Создать ответ от AI (генерация вопроса)
     */
    public void createResponse(WebSocketSession session) throws IOException {
        Map<String, Object> responseMessage = Map.of(
            "type", "response.create",
            "response", Map.of(
                "modalities", new String[]{"audio", "text"},
                "instructions", "Continue the interview by asking the next relevant question."
            )
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMessage)));
        log.info("Requested AI response");
    }

    /**
     * Настроить сессию с инструкциями для интервью
     */
    private void configureSession(WebSocketSession session, String position, 
                                  String jobDescription, String language, 
                                  int questionNumber) throws IOException {
        
        String languageInstruction = getLanguageInstruction(language);
        String systemPrompt = buildSystemPrompt(position, jobDescription, 
                                                languageInstruction, questionNumber);
        
        Map<String, Object> sessionUpdate = Map.of(
            "type", "session.update",
            "session", Map.of(
                "modalities", new String[]{"text", "audio"},
                "instructions", systemPrompt,
                "voice", getVoiceForLanguage(language),
                "input_audio_format", "pcm16",
                "output_audio_format", "pcm16",
                "input_audio_transcription", Map.of("model", "whisper-1"),
                "turn_detection", Map.of(
                    "type", "server_vad",
                    "threshold", 0.5,
                    "prefix_padding_ms", 300,
                    "silence_duration_ms", 500
                )
            )
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(sessionUpdate)));
        log.info("Session configured with system prompt");
    }

    /**
     * Построить системный промпт для интервью
     */
    private String buildSystemPrompt(String position, String jobDescription, 
                                     String languageInstruction, int questionNumber) {
        String questionType = getQuestionTypeForNumber(questionNumber);
        
        return String.format(
            "%s You are a professional interviewer conducting a job interview for the position of %s. " +
            "Job description: %s. " +
            "This is question %d out of 20. %s " +
            "Ask clear, professional questions and listen carefully to the candidate's responses. " +
            "Keep the conversation natural and engaging.",
            languageInstruction, position, jobDescription, questionNumber, questionType
        );
    }

    /**
     * Определить тип вопроса по номеру
     */
    private String getQuestionTypeForNumber(int questionNumber) {
        if (questionNumber <= 5) {
            return "Ask a BACKGROUND question about their experience, education, or career history.";
        } else if (questionNumber <= 13) {
            return "Ask a SITUATIONAL question - present a scenario and ask how they would handle it.";
        } else {
            return "Ask a TECHNICAL question about specific skills or domain knowledge.";
        }
    }

    /**
     * Получить инструкцию по языку
     */
    private String getLanguageInstruction(String language) {
        return switch (language != null ? language.toLowerCase() : "en") {
            case "ru" -> "IMPORTANT: Speak and respond ONLY in Russian language.";
            case "kz" -> "IMPORTANT: Speak and respond ONLY in Kazakh language.";
            default -> "IMPORTANT: Speak and respond ONLY in English language.";
        };
    }

    /**
     * Получить голос для языка
     */
    private String getVoiceForLanguage(String language) {
        return switch (language != null ? language.toLowerCase() : "en") {
            case "ru" -> "alloy"; // OpenAI голоса работают на всех языках
            case "kz" -> "echo";
            default -> "alloy";
        };
    }

    /**
     * Создать headers для WebSocket подключения
     */
    private org.springframework.web.socket.WebSocketHttpHeaders getWebSocketHeaders() {
        org.springframework.web.socket.WebSocketHttpHeaders headers = 
            new org.springframework.web.socket.WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("OpenAI-Beta", "realtime=v1");
        return headers;
    }

    /**
     * Handler для WebSocket сообщений
     */
    public static class RealtimeWebSocketHandler extends TextWebSocketHandler {
        
        private String lastTranscript = "";
        private String lastAudioResponse = "";
        private final StringBuilder audioBuffer = new StringBuilder();
        
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(message.getPayload());
                String type = json.get("type").asText();
                
                log.debug("Received WebSocket message type: {}", type);
                
                switch (type) {
                    case "conversation.item.input_audio_transcription.completed":
                        // Транскрипция пользовательского аудио
                        lastTranscript = json.get("transcript").asText();
                        log.info("Transcription completed: {}", lastTranscript);
                        break;
                        
                    case "response.audio.delta":
                        // Чанк аудио ответа от AI
                        String audioDelta = json.get("delta").asText();
                        audioBuffer.append(audioDelta);
                        break;
                        
                    case "response.audio.done":
                        // Аудио ответ завершен
                        lastAudioResponse = audioBuffer.toString();
                        audioBuffer.setLength(0); // Очистить buffer
                        log.info("Audio response completed, size: {} chars", lastAudioResponse.length());
                        break;
                        
                    case "response.done":
                        log.info("Response generation completed");
                        break;
                        
                    case "error":
                        log.error("WebSocket error: {}", json.get("error"));
                        break;
                }
                
            } catch (Exception e) {
                log.error("Error handling WebSocket message", e);
            }
        }
        
        public String getLastTranscript() {
            return lastTranscript;
        }
        
        public String getLastAudioResponse() {
            return lastAudioResponse;
        }
    }

    /**
     * Класс для хранения информации о Realtime сессии
     */
    public static class RealtimeSession {
        private final WebSocketSession session;
        private final RealtimeWebSocketHandler handler;
        
        public RealtimeSession(WebSocketSession session, RealtimeWebSocketHandler handler) {
            this.session = session;
            this.handler = handler;
        }
        
        public WebSocketSession getSession() {
            return session;
        }
        
        public RealtimeWebSocketHandler getHandler() {
            return handler;
        }
        
        public void close() throws IOException {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}