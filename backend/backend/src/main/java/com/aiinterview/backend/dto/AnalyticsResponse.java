package com.aiinterview.backend.dto;

import java.util.Collections;
import java.util.List;

public class AnalyticsResponse {
    private int totalInterviews;
    private int averageScore;
    private int bestScore;
    private int improvement;
    private List<InterviewSummary> recentInterviews;
    private List<TopicPerformance> topicPerformance;

    public AnalyticsResponse() {
        this.totalInterviews = 0;
        this.averageScore = 0;
        this.bestScore = 0;
        this.improvement = 0;
        this.recentInterviews = Collections.emptyList();
        this.topicPerformance = Collections.emptyList();
    }

    public int getTotalInterviews() {
        return totalInterviews;
    }

    public void setTotalInterviews(int totalInterviews) {
        this.totalInterviews = totalInterviews;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(int averageScore) {
        this.averageScore = averageScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getImprovement() {
        return improvement;
    }

    public void setImprovement(int improvement) {
        this.improvement = improvement;
    }

    public List<InterviewSummary> getRecentInterviews() {
        return recentInterviews;
    }

    public void setRecentInterviews(List<InterviewSummary> recentInterviews) {
        this.recentInterviews = recentInterviews;
    }

    public List<TopicPerformance> getTopicPerformance() {
        return topicPerformance;
    }

    public void setTopicPerformance(List<TopicPerformance> topicPerformance) {
        this.topicPerformance = topicPerformance;
    }

    public static class InterviewSummary {
        private String completedAt;
        private String interviewType;
        private int score;
        private String role;

        public InterviewSummary() {
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public String getInterviewType() {
            return interviewType;
        }

        public void setInterviewType(String interviewType) {
            this.interviewType = interviewType;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class TopicPerformance {
        private String interviewType;
        private int averageScore;

        public TopicPerformance() {
        }

        public String getInterviewType() {
            return interviewType;
        }

        public void setInterviewType(String interviewType) {
            this.interviewType = interviewType;
        }

        public int getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(int averageScore) {
            this.averageScore = averageScore;
        }
    }
}
