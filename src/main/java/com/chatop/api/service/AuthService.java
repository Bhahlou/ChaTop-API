package com.chatop.api.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.chatop.api.dto.GetMeResponse;
import com.chatop.api.dto.LoginRequest;
import com.chatop.api.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);

    String login(LoginRequest request);

    GetMeResponse getMe(UserDetails userDetails);
}
