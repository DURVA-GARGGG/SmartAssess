package com.smartexam.service;

import com.smartexam.dto.AuthRequest;
import com.smartexam.dto.AuthResponse;
import com.smartexam.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}
