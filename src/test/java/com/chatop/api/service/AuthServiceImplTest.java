package com.chatop.api.service;

import com.chatop.api.dto.GetUserResponse;
import com.chatop.api.dto.LoginRequest;
import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.entity.User;
import com.chatop.api.exception.EmailAlreadyExistsException;
import com.chatop.api.exception.LoginException;
import com.chatop.api.repository.UserRepository;
import com.chatop.api.security.JwtUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    @Test
    @Tag("register")
    @DisplayName("register - Success: returns token and saves user with encoded password")
    void registerShouldReturnTokenAndSaveUser() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@test.com", "password123");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtUtil.generateToken("alice@test.com")).thenReturn("jwt-token");

        String token = authService.register(request);

        assertThat(token).isEqualTo("jwt-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Alice");
        assertThat(savedUser.getEmail()).isEqualTo("alice@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @Tag("register")
    @DisplayName("register - Email already exists: throws EmailAlreadyExistsException")
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@test.com", "password123");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    @Tag("login")
    @DisplayName("login - Success: returns token when credentials are valid")
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@test.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("alice@test.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("alice@test.com")).thenReturn("jwt-token");

        String token = authService.login(request);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    @Tag("login")
    @DisplayName("login - User not found: throws LoginException")
    void loginShouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginException.class);

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @Tag("login")
    @DisplayName("login - Wrong password: throws LoginException")
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@test.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("alice@test.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginException.class);

        verify(jwtUtil, never()).generateToken(anyString());
    }

    // -------------------------------------------------------------------------
    // getMe
    // -------------------------------------------------------------------------

    @Test
    @Tag("me")
    @DisplayName("getMe - Success: returns mapped GetUserResponse")
    void getMeShouldReturnMappedResponse() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("alice@test.com");

        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        GetUserResponse expected = new GetUserResponse();
        expected.setId(1L);
        expected.setName("Alice");
        expected.setEmail("alice@test.com");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, GetUserResponse.class)).thenReturn(expected);

        GetUserResponse response = authService.getMe(userDetails);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    @Tag("me")
    @DisplayName("getMe - User not found: throws LoginException")
    void getMeShouldThrowWhenUserNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("unknown@test.com");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe(userDetails))
                .isInstanceOf(LoginException.class);
    }
}
