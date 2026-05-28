package com.chatop.api.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.repository.UserRepository;
import com.chatop.api.service.AuthServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void createUser() {
        authService.register(new RegisterRequest("Alice", "alice@acceptance.com", "password123"));
    }

    @AfterEach
    void cleanUp() {
        userRepository.findByEmail("alice@acceptance.com").ifPresent(userRepository::delete);
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "email": "alice@acceptance.com",
                            "password": "password123"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void loginShouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "email": "pas-un-email",
                            "password": ""
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "email": "nonexistent@user.com",
                            "password": "wrongpassword"
                            }
                        """))
                .andExpect(status().isUnauthorized());
    }

}
