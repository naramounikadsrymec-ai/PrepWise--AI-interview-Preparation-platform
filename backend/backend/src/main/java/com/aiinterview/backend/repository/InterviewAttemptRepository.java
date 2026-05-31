package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.InterviewAttempt;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewAttemptRepository extends JpaRepository<InterviewAttempt, Long> {
    List<InterviewAttempt> findByUserOrderByCompletedAtDesc(User user);
}
