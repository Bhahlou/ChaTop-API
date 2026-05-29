package com.chatop.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.chatop.api.dto.GetUserResponse;
import com.chatop.api.entity.User;
import com.chatop.api.exception.UserNotFoundException;
import com.chatop.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private ModelMapper modelMapper;

    @Test
    @Tag("getUserById")
    @DisplayName("getUserById - Success: returns GetUserResponse when user exists")
    void getUserByIdShouldReturnUserWhenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setName("Alice");
        expectedResponse.setEmail("alice@example.com");
        expectedResponse.setCreatedAt(user.getCreatedAt());
        expectedResponse.setUpdatedAt(user.getUpdatedAt());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, GetUserResponse.class)).thenReturn(expectedResponse);

        var response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(userRepository).findById(1L);
        verify(modelMapper).map(user, GetUserResponse.class);
    }

    @Test
    @Tag("getUserById")
    @DisplayName("getUserById - Failure: throws exception when user does not exist")
    void getUserByIdShouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
