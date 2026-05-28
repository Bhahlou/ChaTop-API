package com.chatop.api.controller;

import com.chatop.api.config.SecurityConfig;
import com.chatop.api.dto.GetMeResponse;
import com.chatop.api.exception.LoginException;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.AuthService;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @BeforeEach
  void setupFilter() throws Exception {
    doAnswer(inv -> {
      inv.getArgument(2, FilterChain.class).doFilter(inv.getArgument(0), inv.getArgument(1));
      return null;
    }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
  }

  @Test
  @Tag("register")
  @DisplayName("POST /api/auth/register - Success")
  void registerShouldReturn200WithTokenWhenValidRequest() throws Exception {
    when(authService.register(any())).thenReturn("jwt-token-test");

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "John Doe",
              "email": "john@test.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token-test"));
  }

  @Test
  @Tag("register")
  @DisplayName("POST /api/auth/register - Invalid Email")
  void registerShouldReturn400WhenEmailIsInvalid() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "John Doe",
              "email": "pas-un-email",
              "password": "password123"
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Tag("register")
  @DisplayName("POST /api/auth/register - Invalid request with blank fields")
  void registerShouldReturn400WhenFieldsAreBlank() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "",
              "email": "",
              "password": ""
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Tag("login")
  @DisplayName("POST /api/auth/login - Success")
  void loginShouldReturn200WithTokenWhenValidCredentials() throws Exception {
    when(authService.login(any())).thenReturn("jwt-token-test");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "john@test.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token-test"));
  }

  @Test
  @Tag("login")
  @DisplayName("POST /api/auth/login - Invalid credentials")
  void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
    when(authService.login(any())).thenThrow(new LoginException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "john@test.com",
              "password": "wrongpassword"
            }
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Tag("me")
  @DisplayName("GET /api/auth/me - Success")
  @WithMockUser(username = "john@test.com")
  void meShouldReturn200WithUserInfoWhenTokenIsValid() throws Exception {
    GetMeResponse meResponse = new GetMeResponse();
    meResponse.setId(1L);
    meResponse.setName("John Doe");
    meResponse.setEmail("john@test.com");
    meResponse.setCreatedAt(LocalDateTime.now());
    meResponse.setUpdatedAt(LocalDateTime.now());
    when(authService.getMe(any())).thenReturn(meResponse);

    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john@test.com"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.created_at").isNotEmpty())
        .andExpect(jsonPath("$.updated_at").isNotEmpty());
  }

  @Test
  @Tag("me")
  @DisplayName("GET /api/auth/me - Missing token")
  void meShouldReturn401WhenTokenIsMissing() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }

}
