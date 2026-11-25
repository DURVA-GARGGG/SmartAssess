package com.smartexam.service;

import com.smartexam.dto.ProctorLogRequest;

public interface ProctorService {
    void logAction(ProctorLogRequest request);
}
