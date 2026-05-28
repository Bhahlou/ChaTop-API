package com.chatop.api.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegisterAcceptanceTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void registerShouldCreateUserAndReturnToken() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "Alice",
              "email": "alice@acceptance.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.token").isString());
  }

  @Test
  void registerShouldReturn400WhenRequestIsInvalid() throws Exception {
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "",
              "email": "pas-un-email",
              "password": ""
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerExistingMailShouldReturn400() throws Exception {
    // First registration
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "Alice",
              "email": "alice@acceptance.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isOk());

    // Second registration with the same email
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "Alice",
              "email": "alice@acceptance.com",
              "password": "password123"
            }
            """))
        .andExpect(status().isBadRequest());
  }

}
