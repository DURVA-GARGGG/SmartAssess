package com.smartexam.service;

import com.smartexam.dto.ProctorLogRequest;

import java.util.List;
import java.util.Map;

public interface ProctorService {
    void logAction(ProctorLogRequest request);

    /** Every attempt with its tab-switch count, flag and result, for the proctor dashboard. */
    List<Map<String, Object>> getAllAttempts();
}
