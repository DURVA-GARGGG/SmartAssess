package com.smartexam.controller;

import com.smartexam.dto.ProctorLogRequest;
import com.smartexam.service.ProctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proctor")
@CrossOrigin(origins = "*")
public class ProctorController {

    private final ProctorService proctorService;

    public ProctorController(ProctorService proctorService) {
        this.proctorService = proctorService;
    }

    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> logAction(@RequestBody ProctorLogRequest request) {
        proctorService.logAction(request);
        return ResponseEntity.ok(Map.of("message", "Proctoring event recorded"));
    }

    // Proctor dashboard: every attempt with tab switches, flag and result (PROCTOR or ADMIN only)
    @GetMapping("/attempts")
    public ResponseEntity<List<Map<String, Object>>> getAttempts() {
        return ResponseEntity.ok(proctorService.getAllAttempts());
    }
}
