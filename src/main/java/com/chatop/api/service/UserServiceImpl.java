package com.chatop.api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.chatop.api.dto.GetUserResponse;
import com.chatop.api.exception.UserNotFoundException;
import com.chatop.api.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public GetUserResponse getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return modelMapper.map(user, GetUserResponse.class);
    }

}
