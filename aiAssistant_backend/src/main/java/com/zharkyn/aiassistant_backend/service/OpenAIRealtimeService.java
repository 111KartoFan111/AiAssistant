package com.zharkyn.aiassistant_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenAIRealtimeService {

    @Value("${openai.api.key}")
    private String apiKey;

    private WebSocketClient client;
    private WebSocketSession session;
    private CountDownLatch responseLatch;
    private StringBuilder aiResponseText;
    private List<String> audioDeltaList;

    @PostConstruct
    public void init() {
        client = new StandardWebSocketClient();
    }

    public String sendAudioAndGetResponse(byte[] audioData) throws IOException {
        aiResponseText = new StringBuilder();
        audioDeltaList = new ArrayList<>();
        responseLatch = new CountDownLatch(1);

        URI uri = URI.create("wss://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview-2024-10-01");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", "realtime=v1");

        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders(headers);

        try {
            session = client.execute(new RealtimeWebSocketHandler(), wsHeaders, uri).get();
            
            log.info("WebSocket connected, sending audio data of size: {} bytes", audioData.length);
            
            // Отправляем аудио через append
            String base64Audio = Base64.getEncoder().encodeToString(audioData);
            
            String appendMessage = String.format(
                "{\"type\":\"input_audio_buffer.append\",\"audio\":\"%s\"}", 
                base64Audio
            );
            session.sendMessage(new TextMessage(appendMessage));
            log.info("Audio data sent");
            Thread.sleep(100);

            // Коммитим буфер
            String commitMessage = "{\"type\":\"input_audio_buffer.commit\"}";
            session.sendMessage(new TextMessage(commitMessage));
            log.info("Audio buffer committed");
            Thread.sleep(100);

            // Запрашиваем ответ
            String responseMessage = "{\"type\":\"response.create\",\"response\":{\"modalities\":[\"text\",\"audio\"]}}";
            session.sendMessage(new TextMessage(responseMessage));
            log.info("Response requested");

            // Ждем ответа максимум 30 секунд
            boolean completed = responseLatch.await(30, TimeUnit.SECONDS);
            
            if (!completed) {
                log.warn("Response timeout - no response received within 30 seconds");
                throw new IOException("Timeout waiting for AI response");
            }

            String response = aiResponseText.toString();
            log.info("Final AI response: {}", response);
            
            return response.isEmpty() ? "Извините, не удалось получить ответ от AI." : response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while waiting for response", e);
            throw new IOException("Interrupted while waiting for AI response", e);
        } catch (Exception e) {
            log.error("Error in WebSocket communication", e);
            throw new IOException("Failed to get AI response: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                    log.info("WebSocket session closed");
                } catch (Exception e) {
                    log.error("Error closing WebSocket session", e);
                }
            }
        }
    }

    private class RealtimeWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("WebSocket connection established successfully");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            String payload = message.getPayload();
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(payload);
                String type = node.get("type").asText();

                log.debug("Received WebSocket message type: {}", type);

                switch (type) {
                    case "session.created":
                        log.info("Session created successfully");
                        break;
                        
                    case "input_audio_buffer.committed":
                        log.info("Audio buffer committed successfully");
                        break;
                        
                    case "response.created":
                        log.info("Response creation started");
                        break;
                        
                    case "response.output_item.added":
                        log.info("Output item added");
                        break;
                        
                    case "response.content_part.added":
                        log.info("Content part added");
                        break;
                        
                    case "response.text.delta":
                        String delta = node.get("delta").asText();
                        aiResponseText.append(delta);
                        log.debug("Text delta received: {}", delta);
                        break;
                        
                    case "response.text.done":
                        log.info("Text response completed");
                        break;
                        
                    case "response.audio.delta":
                        String audioDelta = node.get("delta").asText();
                        audioDeltaList.add(audioDelta);
                        log.debug("Audio delta received");
                        break;
                        
                    case "response.audio.done":
                        log.info("Audio response completed");
                        break;
                        
                    case "response.output_item.done":
                        log.info("Output item completed");
                        break;
                        
                    case "response.done":
                        log.info("Response generation completed successfully");
                        responseLatch.countDown();
                        break;
                        
                    case "error":
                        log.error("WebSocket error received: {}", payload);
                        responseLatch.countDown();
                        break;
                        
                    default:
                        log.debug("Unhandled message type: {}", type);
                }
            } catch (Exception e) {
                log.error("Error processing WebSocket message: {}", payload, e);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket transport error occurred", exception);
            responseLatch.countDown();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.info("WebSocket connection closed with status: {}", status);
            responseLatch.countDown();
        }
    }
}