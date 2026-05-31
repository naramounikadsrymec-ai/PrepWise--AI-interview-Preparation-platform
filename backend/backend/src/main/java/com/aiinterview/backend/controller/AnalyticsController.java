package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.AnalyticsResponse;
import com.aiinterview.backend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"}, allowedHeaders = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/{userId}")
    public AnalyticsResponse getAnalytics(@PathVariable Long userId, Principal principal) {
        return analyticsService.getAnalyticsForUser(userId, principal.getName());
    }

    @GetMapping
    public AnalyticsResponse getCurrentUserAnalytics(Principal principal) {
        return analyticsService.getAnalyticsForCurrentUser(principal.getName());
    }
}
