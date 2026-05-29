package com.chatop.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.config.SecurityConfig;
import com.chatop.api.dto.GetUserResponse;
import com.chatop.api.exception.UserNotFoundException;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.UserService;

import jakarta.servlet.FilterChain;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    @Tag("getUserById")
    void getUserByIdShouldReturn200WhenValidRequest() throws Exception {
        var response = new GetUserResponse();
        response.setId(1L);
        response.setName("John Doe");
        response.setEmail("john.doe@example.com");

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    @Tag("getUserById")
    void getUserByIdShouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("getUserById")
    void getUserByIdShouldReturn401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isUnauthorized());
    }
}