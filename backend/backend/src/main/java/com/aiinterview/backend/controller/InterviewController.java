package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.InterviewEvaluation;
import com.aiinterview.backend.dto.InterviewRequest;
import com.aiinterview.backend.dto.QuestionGenerationResponse;
import com.aiinterview.backend.dto.QuestionRequest;
import com.aiinterview.backend.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"})
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping("/evaluate")
    public InterviewEvaluation evaluateInterview(@RequestBody InterviewRequest request, java.security.Principal principal) {
        return interviewService.evaluateInterview(request, principal.getName());
    }

    @PostMapping(value = "/generate-questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuestionGenerationResponse generateQuestions(
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(value = "targetRole", required = false) String targetRole,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "skills", required = false) String skills) {
        return interviewService.generateInterviewQuestions(resumeFile, targetRole, experience, skills);
    }

    @PostMapping(value = "/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public QuestionGenerationResponse generateQuestions(@RequestBody QuestionRequest request) {
        return interviewService.generateInterviewQuestions(request.getRole(), request.getDifficulty(), request.getCompany(), request.getInterviewType());
    }
}
