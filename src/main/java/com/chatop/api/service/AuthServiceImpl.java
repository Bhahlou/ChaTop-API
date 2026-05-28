package com.chatop.api.service;

import com.chatop.api.dto.GetMeResponse;
import com.chatop.api.dto.LoginRequest;
import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.entity.User;
import com.chatop.api.exception.EmailAlreadyExistsException;
import com.chatop.api.exception.LoginException;
import com.chatop.api.repository.UserRepository;
import com.chatop.api.security.JwtUtil;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String token = jwtUtil.generateToken(request.getEmail());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return token;
    }

    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new LoginException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginException("Invalid credentials");
        }

        return jwtUtil.generateToken(request.getEmail());
    }

    @Override
    public GetMeResponse getMe(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new LoginException("User not found"));

        return modelMapper.map(user, GetMeResponse.class);
    }

}
