package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.AnalyticsResponse;
import com.aiinterview.backend.entity.InterviewAttempt;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewAttemptRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewAttemptRepository interviewAttemptRepository;

    public AnalyticsResponse getAnalyticsForUser(Long userId, String authenticatedEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        }

        return buildAnalyticsResponse(user);
    }

    public AnalyticsResponse getAnalyticsForCurrentUser(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return buildAnalyticsResponse(user);
    }

    private AnalyticsResponse buildAnalyticsResponse(User user) {
        List<InterviewAttempt> attempts = interviewAttemptRepository.findByUserOrderByCompletedAtDesc(user);
        AnalyticsResponse response = new AnalyticsResponse();

        if (attempts.isEmpty()) {
            return response;
        }

        response.setTotalInterviews(attempts.size());

        int totalScore = 0;
        int bestScore = 0;
        List<AnalyticsResponse.InterviewSummary> recent = new ArrayList<>();
        Map<String, List<Integer>> topicBuckets = new HashMap<>();

        for (InterviewAttempt attempt : attempts) {
            if (attempt == null) {
                continue;
            }

            int score = attempt.getScore();
            totalScore += score;
            bestScore = Math.max(bestScore, score);

            AnalyticsResponse.InterviewSummary entry = new AnalyticsResponse.InterviewSummary();
            entry.setCompletedAt(attempt.getCompletedAt() == null ? "Unknown" : attempt.getCompletedAt().toLocalDate().toString());
            entry.setInterviewType(attempt.getInterviewType() == null ? "General" : attempt.getInterviewType());
            entry.setScore(score);
            entry.setRole(attempt.getRole() == null ? "Interview" : attempt.getRole());
            recent.add(entry);

            String topic = attempt.getInterviewType() == null ? "General" : attempt.getInterviewType();
            topicBuckets.computeIfAbsent(topic, key -> new ArrayList<>()).add(score);
        }

        if (!attempts.isEmpty()) {
            response.setAverageScore(Math.round((float) totalScore / attempts.size()));
        } else {
            response.setAverageScore(0);
        }
        response.setBestScore(bestScore);

        int improvement = 0;
        if (attempts.size() > 1) {
            InterviewAttempt latestAttempt = attempts.get(0);
            InterviewAttempt firstAttempt = attempts.get(attempts.size() - 1);
            int latest = latestAttempt != null ? latestAttempt.getScore() : 0;
            int first = firstAttempt != null ? firstAttempt.getScore() : 0;
            if (first > 0) {
                improvement = Math.round(((latest - first) / (float) first) * 100);
            }
        }
        response.setImprovement(improvement);

        response.setRecentInterviews(recent.subList(0, Math.min(5, recent.size())));

        List<AnalyticsResponse.TopicPerformance> performance = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> bucket : topicBuckets.entrySet()) {
            List<Integer> values = bucket.getValue();
            int total = 0;
            for (int score : values) {
                total += score;
            }
            AnalyticsResponse.TopicPerformance topicPerformance = new AnalyticsResponse.TopicPerformance();
            topicPerformance.setInterviewType(bucket.getKey());
            topicPerformance.setAverageScore(Math.round((float) total / values.size()));
            performance.add(topicPerformance);
        }
        response.setTopicPerformance(performance);

        return response;
    }
}
