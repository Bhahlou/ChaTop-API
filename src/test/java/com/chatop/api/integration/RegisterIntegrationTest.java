package com.chatop.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @Tag("register")
  @DisplayName("POST /api/auth/register - Success")
  void registerShouldPersistUserAndReturnToken() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "Bob",
              "email": "bob@integration.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty());
  }

  @Test
  @Tag("register")
  @DisplayName("POST /api/auth/register - Email already exists")
  void registerShouldReturn400WhenEmailAlreadyExists() throws Exception {
    String body = """
        {
          "name": "Bob",
          "email": "duplicate@integration.com",
          "password": "password123"
        }
        """;

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Tag("login")
  @DisplayName("POST /api/auth/login - Success")
  void loginShouldReturn200WithTokenWhenValidCredentials() throws Exception {
    String registerBody = """
        {
          "name": "Alice",
          "email": "alice@integration.com",
          "password": "password123"
        }
        """;

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registerBody))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "alice@integration.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty());
  }
}
