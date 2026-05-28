package com.chatop.api.service;

import com.chatop.api.dto.LoginRequest;
import com.chatop.api.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);

    String login(LoginRequest request);
}
