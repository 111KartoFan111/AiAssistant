package com.zharkyn.aiassistant_backend.service;

import com.zharkyn.aiassistant_backend.dto.GeminiDtos;
import com.zharkyn.aiassistant_backend.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    public Mono<String> generateInitialQuestion(String position, String jobDescription, 
                                                String language, ChatMessage.QuestionType questionType) {
        String languageInstruction = getLanguageInstruction(language);
        String questionTypeInstruction = getQuestionTypeInstruction(questionType);
        
        String prompt = String.format(
                "%s Act as a friendly but professional interviewer. I am applying for the position of %s. " +
                "Here is the job description: '%s'. " +
                "This is question 1 out of 20. %s " +
                "Please ask me the first question to start the interview. Just provide the question text, no preamble.",
                languageInstruction, position, jobDescription, questionTypeInstruction
        );
        return callGemini(List.of(createContent("user", prompt)));
    }

    public Mono<String> generateNextResponse(List<ChatMessage> history, String language,
                                            ChatMessage.QuestionType questionType,
                                            int questionNumber, int totalQuestions) {
        String languageInstruction = getLanguageInstruction(language);
        String questionTypeInstruction = getQuestionTypeInstruction(questionType);
        
        List<GeminiDtos.Content> contents = history.stream()
                .map(msg -> {
                    String geminiRole = msg.getRole().name().equals("USER") ? "user" : "model";
                    return createContent(geminiRole, msg.getContent());
                })
                .collect(Collectors.toList());

        String followUpPrompt = String.format(
            "%s This is question %d out of %d questions. %s " +
            "Continue the interview by asking a relevant question based on my previous answer. " +
            "Keep your questions professional and relevant to the position. Just provide the question, no preamble.",
            languageInstruction, questionNumber, totalQuestions, questionTypeInstruction
        );
        
        contents.add(createContent("user", followUpPrompt));

        GeminiDtos.GeminiRequest request = GeminiDtos.GeminiRequest.builder()
                .contents(contents)
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiDtos.GeminiResponse.class)
                .doOnError(error -> log.error("Error calling Gemini API", error))
                .map(response -> {
                    if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
                        throw new RuntimeException("No response from Gemini API");
                    }
                    return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                });
    }

    private String getLanguageInstruction(String language) {
        switch (language) {
            case "ru":
                return "IMPORTANT: Respond ONLY in Russian language.";
            case "kz":
                return "IMPORTANT: Respond ONLY in Kazakh language.";
            case "en":
            default:
                return "IMPORTANT: Respond ONLY in English language.";
        }
    }

    private String getQuestionTypeInstruction(ChatMessage.QuestionType questionType) {
        switch (questionType) {
            case BACKGROUND:
                return "Ask a BACKGROUND question - about their experience, education, career history, or personal introduction. " +
                       "Example topics: 'Tell me about yourself', 'Describe your work experience', 'Why are you interested in this role?'";
            case SITUATIONAL:
                return "Ask a SITUATIONAL question - present a hypothetical scenario and ask how they would handle it. " +
                       "Example topics: 'How would you handle a conflict with a colleague?', 'What would you do if you missed a deadline?', " +
                       "'Describe how you would approach a challenging project.'";
            case TECHNICAL:
                return "Ask a TECHNICAL question - about specific skills, tools, technologies, or domain knowledge required for the position. " +
                       "Example topics: programming languages, frameworks, methodologies, industry-specific knowledge, problem-solving approaches.";
            default:
                return "";
        }
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
                .doOnError(error -> log.error("Error calling Gemini API", error))
                .map(response -> {
                    if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
                        throw new RuntimeException("No response from Gemini API");
                    }
                    return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                });
    }

    private GeminiDtos.Content createContent(String role, String text) {
        return GeminiDtos.Content.builder()
                .role(role)
                .parts(List.of(GeminiDtos.Part.builder().text(text).build()))
                .build();
    }
    public Mono<String> evaluateAnswer(String evaluationPrompt) {
        GeminiDtos.Content content = createContent("user", evaluationPrompt);
        
        GeminiDtos.GeminiRequest request = GeminiDtos.GeminiRequest.builder()
                .contents(List.of(content))
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiDtos.GeminiResponse.class)
                .doOnError(error -> log.error("Error calling Gemini API for evaluation", error))
                .map(response -> {
                    if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
                        throw new RuntimeException("No response from Gemini API");
                    }
                    return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                });
    }
}