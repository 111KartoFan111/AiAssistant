package com.zharkyn.aiassistant_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIChatService {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api.key:}")
    private String apiKeyProp;

    // Hardcoded for local use; override via OPENAI_API_KEY env or application.properties if set
    private String apiKey = "sk-proj-REPLACE_WITH_YOUR_KEY";

    @Value("${openai.api.base:https://api.openai.com}")
    private String apiBase;

    @Value("${openai.chat.model:gpt-4o-mini}")
    private String chatModel;

    @Value("${openai.stt.model:whisper-1}")
    private String sttModel;

    @PostConstruct
    void initApiKey() {
        if (apiKeyProp != null && !apiKeyProp.isBlank()) {
            this.apiKey = apiKeyProp;
        }
    }

    public String sendAudioAndGetResponse(byte[] audioData) {
        String transcript = transcribe(audioData);
        log.info("Transcript length: {} chars", transcript.length());
        return chatRespond(transcript);
    }

    private String transcribe(byte[] audio) {
        WebClient client = webClientBuilder.baseUrl(apiBase).build();

        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("file", new ByteArrayResource(audio) {
            @Override
            public String getFilename() { return "audio.webm"; }
        }).contentType(MediaType.parseMediaType("audio/webm"));
        mb.part("model", sttModel);
        mb.part("response_format", "text");

        String text = client.post()
                .uri("/v1/audio/transcriptions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(mb.build()))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (text == null || text.isBlank()) {
            throw new RuntimeException("Empty transcription result");
        }
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private String chatRespond(String userText) {
        WebClient client = webClientBuilder.baseUrl(apiBase).build();

        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are a friendly professional interviewer. Reply concisely in the same language as the user."),
                        Map.of("role", "user", "content", userText)
                }
        );

        String response = client.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .map(m -> {
                    try {
                        var choices = (java.util.List<Map<String, Object>>) m.get("choices");
                        if (choices == null || choices.isEmpty()) return "";
                        var msg = (Map<String, Object>) choices.get(0).get("message");
                        return msg != null ? String.valueOf(msg.get("content")) : "";
                    } catch (Exception e) {
                        log.error("Error parsing chat response: {}", m, e);
                        return "";
                    }
                })
                .block();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("Empty chat response");
        }
        return response;
    }
}