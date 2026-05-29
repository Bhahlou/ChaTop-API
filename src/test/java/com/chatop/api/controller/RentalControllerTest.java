package com.chatop.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.config.SecurityConfig;
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.RentalResponse;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.RentalService;

import jakarta.servlet.FilterChain;

@WebMvcTest(RentalController.class)
@Import(SecurityConfig.class)
class RentalControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RentalService rentalService;

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
        @Tag("postRental")
        @DisplayName("POST /api/rentals - Success")
        void postRentalShouldReturn200WithSuccessMessageWhenValidRequest() throws Exception {
                when(rentalService.createRental(any(), any()))
                                .thenReturn(new SuccessMessageResponse("Rental created successfully"));

                MockMultipartFile picture = new MockMultipartFile(
                                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

                mockMvc.perform(multipart("/api/rentals")
                                .file(picture)
                                .param("name", "Test Rental")
                                .param("surface", "100")
                                .param("price", "1500")
                                .param("description", "A nice rental for testing."))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Rental created successfully"));
        }

        @Test
        @Tag("postRental")
        @DisplayName("POST /api/rentals - Missing token")
        void postRentalShouldReturn401WhenTokenIsMissing() throws Exception {
                mockMvc.perform(post("/api/rentals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Test Rental",
                                                    "surface": 100,
                                                    "price": 1500,
                                                    "picture": "http://example.com/rental.jpg",
                                                    "description": "A nice rental for testing."
                                                }
                                                """))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @Tag("getRentals")
        @WithMockUser
        @DisplayName("GET /api/rentals - Success")
        void getRentalsShouldReturn200WithRentalsListWhenValidRequest() throws Exception {
                when(rentalService.getRentals())
                                .thenReturn(new GetAllRentalsResponse(List.of(
                                                new RentalResponse(
                                                                1L,
                                                                "Test Rental 1",
                                                                100,
                                                                1500,
                                                                "http://example.com/rental1.jpg",
                                                                "Description 1",
                                                                1L,
                                                                "2022/01/01",
                                                                "2022/01/01"),
                                                new RentalResponse(
                                                                2L,
                                                                "Test Rental 2",
                                                                200,
                                                                2500,
                                                                "http://example.com/rental2.jpg",
                                                                "Description 2",
                                                                2L,
                                                                "2022/01/01",
                                                                "2022/01/01"))));

                mockMvc.perform(get("/api/rentals"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rentals", hasSize(2)))
                                .andExpect(jsonPath("$.rentals[0].name").value("Test Rental 1"))
                                .andExpect(jsonPath("$.rentals[1].name").value("Test Rental 2"));
        }

}
