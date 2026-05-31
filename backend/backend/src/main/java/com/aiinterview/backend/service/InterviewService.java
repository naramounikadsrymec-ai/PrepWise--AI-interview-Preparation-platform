package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.InterviewEvaluation;
import com.aiinterview.backend.dto.InterviewRequest;
import com.aiinterview.backend.dto.QuestionGenerationResponse;
import com.aiinterview.backend.entity.InterviewAttempt;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewAttemptRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InterviewService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewAttemptRepository interviewAttemptRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String OPENAI_TRANSCRIPTION_URL = "https://api.openai.com/v1/audio/transcriptions";

    public InterviewEvaluation evaluateInterview(InterviewRequest request, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String transcript = request.getTranscript();
        if ((transcript == null || transcript.isBlank()) && request.getAudioBase64() != null) {
            transcript = transcribeAudio(request.getAudioBase64(), request.getAudioMimeType());
        }
        if (transcript == null) {
            transcript = "";
        }

        int fillerCount = countMatches(transcript, "\\b(um|uh|like|you know|actually|basically|so|right)\\b");
        int hesitationCount = countMatches(transcript, "\\b(um|uh|hmm|huh)\\b");
        int communicationScore = deriveScoreFromTranscript(transcript, fillerCount, hesitationCount);
        int confidenceScore = deriveConfidenceScore(request.getRecognitionConfidence(), transcript);

        String prompt = buildInterviewPrompt(request.getQuestion(), request.getAnswerText(), transcript, fillerCount, hesitationCount, confidenceScore, communicationScore);
        String feedback = geminiService.getFeedbackFromPrompt(prompt);

        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setTranscript(transcript);
        evaluation.setFeedback(feedback);
        evaluation.setFillerCount(fillerCount);
        evaluation.setHesitationCount(hesitationCount);
        evaluation.setCommunicationScore(communicationScore);
        evaluation.setConfidenceScore(confidenceScore);
        evaluation.setSuggestions("Use a steady pace, reduce filler words, and keep your answer structured.");

        saveInterviewAttempt(request, user, communicationScore, confidenceScore);

        return evaluation;
    }

    private void saveInterviewAttempt(InterviewRequest request, User user, int communicationScore, int confidenceScore) {
        InterviewAttempt attempt = new InterviewAttempt();
        attempt.setUser(user);
        attempt.setRole(request.getRole());
        attempt.setInterviewType(request.getInterviewType());
        attempt.setQuestion(request.getQuestion());
        attempt.setAnswerText(request.getAnswerText());
        attempt.setTranscript(request.getTranscript());
        attempt.setCommunicationScore(communicationScore);
        attempt.setConfidenceScore(confidenceScore);
        attempt.setScore(Math.round((communicationScore + confidenceScore) / 2f * 10));
        interviewAttemptRepository.save(attempt);
    }

    public QuestionGenerationResponse generateInterviewQuestions(MultipartFile resumeFile, String targetRole, String experience, String skills) {
        String resumeText = null;
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeText = extractTextFromPdf(resumeFile);
        }

        QuestionGenerationResponse response = new QuestionGenerationResponse();
        response.setRole(targetRole == null ? "General software engineering" : targetRole);
        response.setExperience(experience == null ? "Fresher" : experience);
        response.setSkills(skills == null ? "" : skills);
        response.setResumeSummary(resumeText == null ? "" : resumeText);

        String prompt = buildQuestionGenerationPrompt(resumeText, response.getRole(), response.getExperience(), response.getSkills());
        String aiResponse = geminiService.getFeedbackFromPrompt(prompt);
        response.setRawResponse(aiResponse);

        if (isAiErrorResponse(aiResponse)) {
            response.setError("AI quota or service error occurred. Returning fallback interview questions.");
            response.setQuestions(generateFallbackQuestions(response.getRole(), response.getExperience(), response.getSkills(), null, null));
            return response;
        }

        parseQuestionGenerationResponse(aiResponse, response);
        if (response.getQuestions() == null || response.getQuestions().isEmpty()) {
            response.setError("AI returned no usable questions. Returning fallback interview questions.");
            response.setQuestions(generateFallbackQuestions(response.getRole(), response.getExperience(), response.getSkills(), null, null));
        }
        return response;
    }

    public QuestionGenerationResponse generateInterviewQuestions(String targetRole, String difficulty, String company, String interviewType) {
        QuestionGenerationResponse response = new QuestionGenerationResponse();
        response.setRole(targetRole == null ? "General software engineering" : targetRole);
        response.setExperience(difficulty == null ? "Mid" : difficulty);
        response.setSkills((company == null ? "" : company) + (interviewType == null ? "" : " - " + interviewType));
        response.setResumeSummary("");

        String prompt = buildQuestionGenerationPrompt(null, response.getRole(), response.getExperience(), response.getSkills(), company, interviewType);
        String aiResponse = geminiService.getFeedbackFromPrompt(prompt);
        response.setRawResponse(aiResponse);

        if (isAiErrorResponse(aiResponse)) {
            response.setError("AI quota or service error occurred. Returning fallback interview questions.");
            response.setQuestions(generateFallbackQuestions(response.getRole(), response.getExperience(), response.getSkills(), company, interviewType));
            return response;
        }

        parseQuestionGenerationResponse(aiResponse, response);
        if (response.getQuestions() == null || response.getQuestions().isEmpty()) {
            response.setError("AI returned no usable questions. Returning fallback interview questions.");
            response.setQuestions(generateFallbackQuestions(response.getRole(), response.getExperience(), response.getSkills(), company, interviewType));
        }
        return response;
    }

    private String validateAiApiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service returned no response.");
        }

        if (aiResponse.startsWith("Error:")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, aiResponse);
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResponse);
            if (root.has("error")) {
                JsonNode errorNode = root.get("error");
                String message = errorNode.path("message").asText("AI service error");
                String status = errorNode.path("status").asText();
                int code = errorNode.path("code").asInt(0);

                if ("RESOURCE_EXHAUSTED".equalsIgnoreCase(status) || code == 429 || message.toLowerCase().contains("quota")) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
                }

                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
            }
        } catch (Exception ignored) {
            // Not a JSON error response, continue with the raw text.
        }

        return aiResponse;
    }

    private void parseQuestionGenerationResponse(String aiResponse, QuestionGenerationResponse response) {
        String jsonBlock = extractJsonBlock(aiResponse);
        if (jsonBlock != null) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(jsonBlock);
                response.setRole(root.path("role").asText(response.getRole()));
                response.setExperience(root.path("experience").asText(response.getExperience()));
                response.setSkills(root.path("skills").asText(response.getSkills()));
                response.setQuestions(readStringArray(root.path("questions")));
                return;
            } catch (Exception ignored) {
                // fall back to heuristic parsing
            }
        }
        response.setQuestions(extractQuestions(aiResponse));
    }

    private boolean isAiErrorResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return true;
        }
        if (aiResponse.startsWith("Error:")) {
            return true;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResponse);
            return root.has("error") || root.path("status").asText().equalsIgnoreCase("RESOURCE_EXHAUSTED");
        } catch (Exception ignored) {
            return false;
        }
    }

    private List<String> generateFallbackQuestions(String role, String experience, String skills, String company, String interviewType) {
        List<String> questions = new ArrayList<>();
        questions.add("Describe a recent technical challenge you solved and how you approached it.");
        questions.add("How do you prioritize tasks when working on competing deadlines?");
        questions.add("Explain a time you worked with a team to deliver a project successfully.");
        questions.add("What strengths do you bring to a role as " + role + "?");

        if (interviewType != null) {
            String lowerType = interviewType.toLowerCase();
            if (lowerType.contains("system")) {
                questions.add("How would you design a scalable system to handle growing user traffic?");
            } else if (lowerType.contains("behavioral")) {
                questions.add("Tell me about a time when you handled a difficult stakeholder or team conflict.");
            } else if (lowerType.contains("culture")) {
                questions.add("How do you adapt to new team cultures and support collaboration?");
            } else {
                questions.add("How do you stay up to date with new technologies in your field?");
            }
        } else {
            questions.add("How do you stay up to date with new technologies in your field?");
        }

        if (company != null && !company.isBlank()) {
            questions.add("Why are you interested in working at " + company + " and what would you contribute?");
        }

        return questions;
    }

    private String buildQuestionGenerationPrompt(String resumeText, String targetRole, String experience, String skills) {
        return buildQuestionGenerationPrompt(resumeText, targetRole, experience, skills, null, null);
    }

    private String buildQuestionGenerationPrompt(String resumeText, String targetRole, String experience, String skills, String company, String interviewType) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are an AI interview question generator.\n");
        builder.append("Create a short list of interview questions for a candidate based on the following information.\n");
        builder.append("Return JSON only with keys: questions (array of strings), role, experience, skills.\n");
        builder.append("Target role: ").append(targetRole == null ? "General software engineering" : targetRole).append("\n");
        builder.append("Experience level: ").append(experience == null ? "Fresher" : experience).append("\n");
        builder.append("Skills: ").append(skills == null ? "None" : skills).append("\n");
        if (company != null && !company.isBlank()) {
            builder.append("Company: ").append(company).append("\n");
        }
        if (interviewType != null && !interviewType.isBlank()) {
            builder.append("Interview type: ").append(interviewType).append("\n");
        }
        if (resumeText != null && !resumeText.isBlank()) {
            builder.append("Resume summary text: \n").append(resumeText).append("\n");
        }
        builder.append("Generate at least 5 questions, including technical, HR, and coding topics.\n");
        builder.append("Example output format:\n");
        builder.append("{\n");
        builder.append("  \"role\": \"Java Full Stack\",\n");
        builder.append("  \"experience\": \"Fresher\",\n");
        builder.append("  \"skills\": \"Java, Spring, REST\",\n");
        builder.append("  \"questions\": [\"Explain REST API lifecycle\", \"Difference between JPA and Hibernate\", \"Build JWT authentication flow\"]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private List<String> extractQuestions(String aiResponse) {
        String jsonBlock = extractJsonBlock(aiResponse);
        if (jsonBlock != null) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(jsonBlock);
                return readStringArray(root.path("questions"));
            } catch (Exception ignored) {
                // fall back to heuristic parsing
            }
        }

        List<String> questions = new ArrayList<>();
        Pattern pattern = Pattern.compile("[0-9]+\\.\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(aiResponse);
        while (matcher.find()) {
            questions.add(matcher.group(1).trim());
        }
        if (questions.isEmpty()) {
            Pattern jsonPattern = Pattern.compile("\\\"questions\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Matcher jsonMatcher = jsonPattern.matcher(aiResponse);
            if (jsonMatcher.find()) {
                String arrayContent = jsonMatcher.group(1);
                Pattern itemPattern = Pattern.compile("\\\"(.*?)\\\"", Pattern.DOTALL);
                Matcher itemMatcher = itemPattern.matcher(arrayContent);
                while (itemMatcher.find()) {
                    questions.add(itemMatcher.group(1).trim());
                }
            }
        }
        return questions;
    }

    private String extractTextFromPdf(MultipartFile resumeFile) {
        try (InputStream inputStream = resumeFile.getInputStream(); PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String extractJsonBlock(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Pattern fencedPattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher fencedMatcher = fencedPattern.matcher(text);
        if (fencedMatcher.find()) {
            return fencedMatcher.group(1);
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private List<String> readStringArray(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode node : arrayNode) {
                if (node.isTextual()) {
                    values.add(node.asText());
                }
            }
        }
        return values;
    }

    private int countMatches(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int deriveScoreFromTranscript(String transcript, int fillerCount, int hesitationCount) {
        int base = 8;
        base -= Math.min(fillerCount, 4);
        base -= Math.min(hesitationCount, 3);
        if (transcript.isBlank()) {
            return 2;
        }
        return Math.max(1, Math.min(10, base));
    }

    private int deriveConfidenceScore(Double recognitionConfidence, String transcript) {
        if (recognitionConfidence != null) {
            return (int) Math.round(Math.min(1.0, Math.max(0.0, recognitionConfidence)) * 10);
        }
        if (transcript.length() > 120) {
            return 8;
        }
        if (transcript.length() > 60) {
            return 6;
        }
        return 5;
    }

    private String buildInterviewPrompt(String question, String answerText, String transcript, int fillerCount, int hesitationCount, int confidenceScore, int communicationScore) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are an interview coach.\n");
        builder.append("Question: ").append(question != null ? question : "").append("\n");
        builder.append("Transcript: ").append(transcript).append("\n");
        builder.append("Answer text: ").append(answerText != null ? answerText : transcript).append("\n");
        builder.append("Filler word count: ").append(fillerCount).append("\n");
        builder.append("Hesitation count: ").append(hesitationCount).append("\n");
        builder.append("Recognition confidence score: ").append(confidenceScore).append("/10\n");
        builder.append("Communication estimate: ").append(communicationScore).append("/10\n");
        builder.append("Provide a concise evaluation of the answer focusing on confidence, hesitation, filler words, communication clarity, and suggestions for improvement. Include final scores for Communication and Confidence out of 10.");
        return builder.toString();
    }

    private String transcribeAudio(String audioBase64, String mimeType) {
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey == null || openAiKey.isBlank()) {
            return "";
        }

        try {
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            String boundary = "----OpenAIFormBoundary" + System.currentTimeMillis();
            URL url = new URL(OPENAI_TRANSCRIPTION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + openAiKey);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            try (DataOutputStream output = new DataOutputStream(conn.getOutputStream())) {
                writeFormField(output, boundary, "model", "whisper-1");
                writeFileField(output, boundary, "file", "audio.webm", mimeType != null ? mimeType : "audio/webm", audioBytes);
                output.writeBytes("--" + boundary + "--\r\n");
            }

            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            if (responseCode != 200) {
                return "";
            }

            String responseJson = responseBuilder.toString();
            int textIndex = responseJson.indexOf("\"text\"");
            if (textIndex < 0) {
                return "";
            }
            int colonIndex = responseJson.indexOf(":", textIndex);
            int quoteStart = responseJson.indexOf('"', colonIndex + 1);
            int quoteEnd = responseJson.indexOf('"', quoteStart + 1);
            if (quoteStart < 0 || quoteEnd < 0) {
                return "";
            }
            return responseJson.substring(quoteStart + 1, quoteEnd).replaceAll("\\\\n", " ");
        } catch (Exception e) {
            return "";
        }
    }

    private void writeFormField(DataOutputStream output, String boundary, String name, String value) throws Exception {
        output.writeBytes("--" + boundary + "\r\n");
        output.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        output.writeBytes(value + "\r\n");
    }

    private void writeFileField(DataOutputStream output, String boundary, String name, String filename, String contentType, byte[] bytes) throws Exception {
        output.writeBytes("--" + boundary + "\r\n");
        output.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
        output.writeBytes("Content-Type: " + contentType + "\r\n\r\n");
        output.write(bytes);
        output.writeBytes("\r\n");
    }
}
