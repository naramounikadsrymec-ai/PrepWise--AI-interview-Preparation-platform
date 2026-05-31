package com.aiinterview.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeminiService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String DEFAULT_API_KEY =
            "AIzaSyDQPszeNAMmdK6aW7gi0amMR9JCajR71Zg";

    @Value("${gemini.api.key:AIzaSyDQPszeNAMmdK6aW7gi0amMR9JCajR71Zg}")
    private String configuredApiKey;

    public String getFeedback(String question, String answer) {

        String prompt = "Question: " + (question == null ? "" : question)
                + "\nAnswer: " + (answer == null ? "" : answer)
                + "\nGive short interview feedback. Include scores for Communication and Technical Accuracy out of 10.";

        return getFeedbackFromPrompt(prompt);
    }

    public String getFeedbackFromPrompt(String prompt) {
        try {
            URL url = new URL(ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = configuredApiKey != null ? configuredApiKey : DEFAULT_API_KEY;
            }

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", apiKey);
            conn.setDoOutput(true);

            ObjectNode textPart = OBJECT_MAPPER.createObjectNode();
            textPart.put("text", prompt);

            ObjectNode contentObject = OBJECT_MAPPER.createObjectNode();
            contentObject.put("role", "user");
            contentObject.set("parts", OBJECT_MAPPER.createArrayNode().add(textPart));

            ArrayNode contentsArray = OBJECT_MAPPER.createArrayNode().add(contentObject);

            ObjectNode requestBody = OBJECT_MAPPER.createObjectNode();
            requestBody.set("contents", contentsArray);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(OBJECT_MAPPER.writeValueAsBytes(requestBody));
            }

            BufferedReader br;
            if (conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder responseBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                responseBuilder.append(output);
            }
            br.close();

            JsonNode jsonResponse = OBJECT_MAPPER.readTree(responseBuilder.toString());

            if (conn.getResponseCode() != 200) {
                return jsonResponse.toString();
            }

            JsonNode feedbackNode = jsonResponse.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (feedbackNode.isMissingNode() || feedbackNode.isNull()) {
                return "No feedback text returned from Gemini.";
            }

            return feedbackNode.asText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}