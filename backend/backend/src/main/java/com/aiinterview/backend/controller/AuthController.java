package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.JwtResponse;
import com.aiinterview.backend.dto.LoginRequest;
import com.aiinterview.backend.dto.RegisterRequest;
import com.aiinterview.backend.dto.UserInfoResponse;
import com.aiinterview.backend.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"}, allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    // REGISTER API
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    // LOGIN API
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> currentUser(java.security.Principal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getName()));
    }
}