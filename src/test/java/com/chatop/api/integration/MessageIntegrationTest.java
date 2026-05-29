package com.chatop.api.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.repository.UserRepository;
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.AuthService;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.FilterChain;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageIntegrationTest {

    private static final String TEST_USER_EMAIL = "alice@integration.com";

    @TempDir
    static Path tempUploadDir;

    @DynamicPropertySource
    static void uploadDir(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> tempUploadDir.toString());
    }

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

    @BeforeEach
    void setUp() throws Exception {
        authService.register(new RegisterRequest("Alice", TEST_USER_EMAIL, "password123"));
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("post")
    @DisplayName("POST /api/messages - Success")
    void sendMessageShouldReturn200WithSuccessMessage() throws Exception {
        Long rentalId = createRentalAndGetId("Test Rental");
        Long userId = userRepository.findByEmail(TEST_USER_EMAIL).orElseThrow().getId();

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rental_id": %d,
                            "user_id": %d,
                            "message": "Hello, is this item still available?"
                        }
                        """.formatted(rentalId, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message send with success !"));
    }

    @Test
    @Tag("post")
    @DisplayName("POST /api/messages - Missing token")
    void sendMessageShouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rental_id": 1,
                            "user_id": 1,
                            "message": "Hello"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("post")
    @DisplayName("POST /api/messages - Invalid request (negative IDs)")
    void sendMessageShouldReturn400WhenRequestIsInvalid() throws Exception {
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

    private Long createRentalAndGetId(String name) throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api/rentals")
                .file(picture)
                .param("name", name)
                .param("surface", "80")
                .param("price", "1200")
                .param("description", "A lovely place."))
                .andExpect(status().isOk());

        var response = mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.rentals[0].id")).longValue();
    }
}
