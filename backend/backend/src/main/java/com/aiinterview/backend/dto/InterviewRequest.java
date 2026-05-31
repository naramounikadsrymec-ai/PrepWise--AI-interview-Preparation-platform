package com.aiinterview.backend.dto;

public class InterviewRequest {
    private String question;
    private String answerText;
    private String transcript;
    private String audioBase64;
    private String audioMimeType;
    private String webcamSnapshotBase64;
    private Double recognitionConfidence;
    private String role;
    private String interviewType;

    public InterviewRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getAudioMimeType() {
        return audioMimeType;
    }

    public void setAudioMimeType(String audioMimeType) {
        this.audioMimeType = audioMimeType;
    }

    public String getWebcamSnapshotBase64() {
        return webcamSnapshotBase64;
    }

    public void setWebcamSnapshotBase64(String webcamSnapshotBase64) {
        this.webcamSnapshotBase64 = webcamSnapshotBase64;
    }

    public Double getRecognitionConfidence() {
        return recognitionConfidence;
    }

    public void setRecognitionConfidence(Double recognitionConfidence) {
        this.recognitionConfidence = recognitionConfidence;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }
}
