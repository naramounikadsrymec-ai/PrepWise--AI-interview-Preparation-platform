package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.CodeExecutionRequest;
import com.aiinterview.backend.dto.CodeExecutionResponse;
import com.aiinterview.backend.dto.CodeExecutionResult;
import com.aiinterview.backend.service.Judge0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"})
public class CodeExecutionController {

    @Autowired
    private Judge0Service judge0Service;

    @PostMapping("/execute")
    public CodeExecutionResponse executeCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse response = new CodeExecutionResponse();

        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            response.setStatus("error");
            response.setMessage("Source code is required.");
            response.setResults(Collections.emptyList());
            return response;
        }

        List<CodeExecutionResult> results = new ArrayList<>();
        List<String> testCases = request.getTestCases();
        if (testCases != null && !testCases.isEmpty()) {
            for (String input : testCases) {
                CodeExecutionResult execution = judge0Service.execute(request.getSourceCode(), request.getLanguage(), input);
                execution.setInput(input == null ? "" : input);
                results.add(execution);
            }
            response.setStatus("ok");
            response.setMessage("Test execution completed.");
            response.setResults(results);
            return response;
        }

        String input = request.getCustomInput() == null ? "" : request.getCustomInput();
        CodeExecutionResult execution = judge0Service.execute(request.getSourceCode(), request.getLanguage(), input);
        execution.setInput(input);
        results.add(execution);
        response.setStatus("ok");
        response.setMessage("Execution completed.");
        response.setResults(results);
        return response;
    }
}
