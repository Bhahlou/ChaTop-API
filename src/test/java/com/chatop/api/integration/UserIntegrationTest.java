package com.chatop.api.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.repository.UserRepository;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.AuthService;

import jakarta.servlet.FilterChain;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    private static final String TEST_USER_EMAIL = "alice@acceptance.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Long id;

    @BeforeEach
    void setUp() throws Exception {
        authService.register(new RegisterRequest("Alice", TEST_USER_EMAIL, "password123"));
        id = userRepository.findByEmail(TEST_USER_EMAIL).get().getId();
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("getUserById")
    void getUserByIdShouldReturn200WhenUserExists() throws Exception {
        mockMvc.perform(get("/api/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_USER_EMAIL))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("getUserById")
    void getUserByIdShouldReturn404WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/user/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("getUserById")
    void getUserByIdShouldReturn401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/{id}", id))
                .andExpect(status().isUnauthorized());
    }
}
