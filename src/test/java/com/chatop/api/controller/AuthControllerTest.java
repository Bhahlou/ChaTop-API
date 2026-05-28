package com.chatop.api.controller;

import com.chatop.api.exception.LoginException;
import com.chatop.api.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

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

}
