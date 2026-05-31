package com.aiinterview.backend.dto;

import java.util.List;

public class ResumeAnalysisResponse {

    private int atsScore;
    private List<String> missingSkills;
    private List<String> weakBulletPoints;
    private String grammarIssues;
    private int roleMatchPercentage;
    private String summary;
    private String rawAnalysis;

    public ResumeAnalysisResponse() {
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getWeakBulletPoints() {
        return weakBulletPoints;
    }

    public void setWeakBulletPoints(List<String> weakBulletPoints) {
        this.weakBulletPoints = weakBulletPoints;
    }

    public String getGrammarIssues() {
        return grammarIssues;
    }

    public void setGrammarIssues(String grammarIssues) {
        this.grammarIssues = grammarIssues;
    }

    public int getRoleMatchPercentage() {
        return roleMatchPercentage;
    }

    public void setRoleMatchPercentage(int roleMatchPercentage) {
        this.roleMatchPercentage = roleMatchPercentage;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRawAnalysis() {
        return rawAnalysis;
    }

    public void setRawAnalysis(String rawAnalysis) {
        this.rawAnalysis = rawAnalysis;
    }
}
