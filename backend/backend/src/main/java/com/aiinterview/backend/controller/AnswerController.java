package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.Answer;
import com.aiinterview.backend.repository.AnswerRepository;
import com.aiinterview.backend.service.GeminiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"})
public class AnswerController {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private GeminiService geminiService;

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    @PostMapping
    public Answer saveAnswer(@RequestBody Answer answer) {
        return answerRepository.save(answer);
    }

    @PostMapping("/save")
    public List<Answer> saveAnswers(@RequestBody List<Answer> answers) {
        return answerRepository.saveAll(answers);
    }

    @PostMapping("/ai-feedback")
    public Map<String, String> getAIFeedback(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");
        String answer = request.get("answer");

        String feedback =
                geminiService.getFeedback(question, answer);

        Map<String, String> response = new HashMap<>();

        response.put("feedback", feedback);

        return response;
    }
}