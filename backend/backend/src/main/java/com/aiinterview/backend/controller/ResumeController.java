package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.ResumeAnalysisResponse;
import com.aiinterview.backend.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3003"})
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeAnalysisResponse analyzeResume(
            @RequestPart("resumeFile") MultipartFile resumeFile,
            @RequestParam(value = "targetRole", required = false) String targetRole) {
        return resumeService.analyzeResume(resumeFile, targetRole);
    }
}
