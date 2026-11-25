package com.smartexam.controller;

import com.smartexam.dto.CodeRunRequest;
import com.smartexam.dto.CodeRunResponse;
import com.smartexam.dto.CodeSubmitRequest;
import com.smartexam.dto.CodeSubmitResponse;
import com.smartexam.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/code")
@CrossOrigin(origins = "*")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<CodeRunResponse> runCode(@RequestBody CodeRunRequest request) {
        CodeRunResponse response = codeExecutionService.runCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<CodeSubmitResponse> submitCode(@RequestBody CodeSubmitRequest request) {
        CodeSubmitResponse response = codeExecutionService.submitCode(request);
        return ResponseEntity.ok(response);
    }
}
