package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.ResumeAnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private GeminiService geminiService;

    public ResumeAnalysisResponse analyzeResume(MultipartFile resumeFile, String targetRole) {
        String resumeText = extractTextFromPdf(resumeFile);

        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setRawAnalysis("Unable to analyze resume because the PDF text could not be extracted.");

        if (resumeText == null || resumeText.isBlank()) {
            return response;
        }

        String prompt = buildResumePrompt(resumeText, targetRole);
        String aiResponse = geminiService.getFeedbackFromPrompt(prompt);
        response.setRawAnalysis(aiResponse);

        parseAiResponse(aiResponse, response);
        return response;
    }

    private String extractTextFromPdf(MultipartFile resumeFile) {
        try {
            byte[] bytes = resumeFile.getBytes();
            try (PDDocument document = PDDocument.load(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String buildResumePrompt(String resumeText, String targetRole) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a resume and career assistant.\n");
        builder.append("Analyze the resume text for ATS compatibility, missing skills, weak bullet points, grammar issues, and role fit.\n");
        builder.append("Return valid JSON only with the following fields:\n");
        builder.append("  atsScore (integer 0-100),\n");
        builder.append("  missingSkills (array of strings),\n");
        builder.append("  weakBulletPoints (array of strings),\n");
        builder.append("  grammarIssues (string),\n");
        builder.append("  roleMatchPercentage (integer 0-100),\n");
        builder.append("  summary (string)\n");
        builder.append("Do not include any other keys.\n");
        builder.append("Target role: ").append(targetRole == null ? "General software engineering" : targetRole).append("\n");
        builder.append("Resume text:\n");
        builder.append(resumeText);
        builder.append("\n");
        return builder.toString();
    }

    private void parseAiResponse(String aiResponse, ResumeAnalysisResponse response) {
        String jsonBlock = extractJsonBlock(aiResponse);
        if (jsonBlock != null) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(jsonBlock);
                if (root.isObject()) {
                    response.setAtsScore(root.path("atsScore").asInt(0));
                    response.setRoleMatchPercentage(root.path("roleMatchPercentage").asInt(0));
                    response.setGrammarIssues(root.path("grammarIssues").asText(""));
                    response.setSummary(root.path("summary").asText(""));
                    response.setMissingSkills(readStringArray(root.path("missingSkills")));
                    response.setWeakBulletPoints(readStringArray(root.path("weakBulletPoints")));
                    return;
                }
            } catch (Exception ignored) {
                // fall back to heuristic parsing
            }
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResponse);
            if (root.isObject()) {
                response.setAtsScore(root.path("atsScore").asInt(0));
                response.setRoleMatchPercentage(root.path("roleMatchPercentage").asInt(0));
                response.setGrammarIssues(root.path("grammarIssues").asText(""));
                response.setSummary(root.path("summary").asText(""));
                response.setMissingSkills(readStringArray(root.path("missingSkills")));
                response.setWeakBulletPoints(readStringArray(root.path("weakBulletPoints")));
                return;
            }
        } catch (Exception ignored) {
            // fall back to heuristic parsing
        }

        response.setAtsScore(extractInteger(aiResponse, "ATS Score"));
        response.setRoleMatchPercentage(extractInteger(aiResponse, "Role Match"));
        response.setGrammarIssues(extractSection(aiResponse, "Grammar Issues"));
        response.setSummary(extractSection(aiResponse, "Summary"));
        response.setMissingSkills(extractList(aiResponse, "Missing Skills"));
        response.setWeakBulletPoints(extractList(aiResponse, "Weak Bullet Points"));
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

    private int extractInteger(String text, String label) {
        Pattern pattern = Pattern.compile(label + ".*?(\\d{1,3})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private String extractSection(String text, String label) {
        Pattern pattern = Pattern.compile(label + ":?\\s*(.*?)(?:\\n\\s*\\n|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private List<String> extractList(String text, String label) {
        String section = extractSection(text, label);
        List<String> results = new ArrayList<>();
        if (section.isBlank()) {
            return results;
        }
        Pattern pattern = Pattern.compile("[-*•]\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(section);
        while (matcher.find()) {
            results.add(matcher.group(1).trim());
        }
        if (results.isEmpty()) {
            String[] parts = section.split("[,;\\n]");
            for (String part : parts) {
                String item = part.trim();
                if (!item.isBlank()) {
                    results.add(item);
                }
            }
        }
        return results;
    }
}
