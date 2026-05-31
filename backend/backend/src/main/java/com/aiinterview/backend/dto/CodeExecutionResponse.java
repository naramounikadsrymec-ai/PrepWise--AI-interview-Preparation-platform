package com.aiinterview.backend.dto;

import java.util.List;

public class CodeExecutionResponse {

    private String status;
    private String message;
    private List<CodeExecutionResult> results;

    public CodeExecutionResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CodeExecutionResult> getResults() {
        return results;
    }

    public void setResults(List<CodeExecutionResult> results) {
        this.results = results;
    }
}
