package com.smartexam.service;

import com.smartexam.dto.CodeRunRequest;
import com.smartexam.dto.CodeRunResponse;
import com.smartexam.dto.CodeSubmitRequest;
import com.smartexam.dto.CodeSubmitResponse;

public interface CodeExecutionService {
    CodeRunResponse runCode(CodeRunRequest request);
    CodeSubmitResponse submitCode(CodeSubmitRequest request);
}
