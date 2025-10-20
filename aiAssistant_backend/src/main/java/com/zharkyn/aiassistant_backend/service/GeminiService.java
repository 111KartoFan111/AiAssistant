package com.zharkyn.aiassistant_backend.service;

import com.zharkyn.aiassistant_backend.dto.GeminiDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiService(WebClient.Builder webClientBuilder,
                         @Value("${gemini.api.url}") String apiUrl,
                         @Value("${gemini.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
    }

    public Mono<String> generateInitialQuestion(String position, String jobDescription) {
        String prompt = String.format(
                "Act as a friendly but professional interviewer. I am applying for the position of %s. " +
                "Here is the job description: '%s'. " +
                "Please ask me the first question to start the interview. Make it a common opening question, " +
                "like 'Tell me about yourself' or 'Why are you interested in this role?'. Just provide the question text, no preamble.",
                position, jobDescription
        );
        return callGemini(List.of(createContent("user", prompt)));
    }

    public Mono<String> generateNextResponse(List<com.zharkyn.aiassistant_backend.model.ChatMessage> history) {
        List<GeminiDtos.Content> contents = history.stream()
                .map(msg -> createContent(msg.getRole().name().toLowerCase(), msg.getContent()))
                .collect(Collectors.toList());

        GeminiDtos.GeminiRequest request = GeminiDtos.GeminiRequest.builder()
                .contents(contents)
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiDtos.GeminiResponse.class)
                .map(response -> response.getCandidates().get(0).getContent().getParts().get(0).getText());
    }

    private Mono<String> callGemini(List<GeminiDtos.Content> contents) {
        GeminiDtos.GeminiRequest request = GeminiDtos.GeminiRequest.builder()
                .contents(contents)
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiDtos.GeminiResponse.class)
                .map(response -> response.getCandidates().get(0).getContent().getParts().get(0).getText());
    }

    private GeminiDtos.Content createContent(String role, String text) {
        return GeminiDtos.Content.builder()
                .role(role)
                .parts(List.of(GeminiDtos.Part.builder().text(text).build()))
                .build();
    }
}
