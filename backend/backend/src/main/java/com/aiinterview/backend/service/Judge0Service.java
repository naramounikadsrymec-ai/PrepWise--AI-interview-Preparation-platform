package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.CodeExecutionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final WebClient webClient;
    private final Map<String, Integer> languageMap = new HashMap<>();

    public Judge0Service(@Value("${judge0.base-url:http://localhost:2358}") String baseUrl,
                         @Value("${judge0.api-key:}") String apiKey) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json");

        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-RapidAPI-Key", apiKey);
        }

        this.webClient = builder.build();
        languageMap.put("java", 62);
        languageMap.put("python", 71);
        languageMap.put("cpp", 54);
    }

    public CodeExecutionResult execute(String sourceCode, String language, String stdin) {
        CodeExecutionResult result = new CodeExecutionResult();
        try {
            Integer languageId = languageMap.get(language);
            if (languageId == null) {
                result.setStatus("Unsupported language");
                result.setStderr("Language not supported: " + language);
                return result;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("source_code", sourceCode);
            payload.put("language_id", languageId);
            payload.put("stdin", stdin == null ? "" : stdin);
            payload.put("wait", true);
            payload.put("base64_encoded", false);

            String responseBody = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/submissions").queryParam("wait", "true").queryParam("base64_encoded", "false").build())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                result.setStatus("No response from Judge0");
                return result;
            }

            JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);

            result.setStdout(responseJson.path("stdout").asText(""));
            result.setStderr(responseJson.path("stderr").asText(""));
            result.setCompileOutput(responseJson.path("compile_output").asText(""));
            result.setStatus(responseJson.path("status").path("description").asText("Unknown"));
            result.setTime(responseJson.path("time").asText("N/A"));
            result.setMemory(responseJson.path("memory").asText("N/A"));
        } catch (WebClientResponseException ex) {
            result.setStatus("Judge0 request failed");
            result.setStderr(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            result.setStatus("Execution error");
            result.setStderr(ex.getMessage());
        }
        return result;
    }
}
