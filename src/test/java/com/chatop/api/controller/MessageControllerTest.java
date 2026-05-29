package com.chatop.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.config.SecurityConfig;
import com.chatop.api.dto.MessageRequest;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.MessageService;

import jakarta.servlet.FilterChain;

@WebMvcTest(MessageController.class)
@Import(SecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

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
    @WithMockUser
    @Tag("postMessage")
    @DisplayName("POST /api/messages - Success")
    void postMessageShouldReturn200WhenValidRequest() throws Exception {
        when(messageService.sendMessage(any(MessageRequest.class), eq("user")))
                .thenReturn(new SuccessMessageResponse("Message send with success !"));

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rental_id": 1,
                            "user_id": 1,
                            "message": "Hello, is this item still available?"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message send with success !"));
    }

    @Test
    @Tag("postMessage")
    @DisplayName("POST /api/messages - Missing token")
    void postMessageShouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rental_id": 1,
                            "user_id": 1,
                            "message": "Hello, is this item still available?"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @Tag("postMessage")
    @DisplayName("POST /api/messages - Invalid request (negative IDs)")
    void postMessageShouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rental_id": -1,
                            "user_id": -1,
                            "message": "Hello"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
}
