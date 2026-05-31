package com.aiinterview.backend.dto;

public class InterviewEvaluation {
    private String transcript;
    private String feedback;
    private int fillerCount;
    private int hesitationCount;
    private int confidenceScore;
    private int communicationScore;
    private String suggestions;

    public InterviewEvaluation() {
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getFillerCount() {
        return fillerCount;
    }

    public void setFillerCount(int fillerCount) {
        this.fillerCount = fillerCount;
    }

    public int getHesitationCount() {
        return hesitationCount;
    }

    public void setHesitationCount(int hesitationCount) {
        this.hesitationCount = hesitationCount;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public int getCommunicationScore() {
        return communicationScore;
    }

    public void setCommunicationScore(int communicationScore) {
        this.communicationScore = communicationScore;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
}
