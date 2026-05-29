package com.chatop.api.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.entity.Rental;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.service.AuthService;
import com.jayway.jsonpath.JsonPath;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostMessageAcceptanceTest {

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
    private RentalRepository rentalRepository;

    @BeforeEach
    void setUp() {
        authService.register(new RegisterRequest("Alice", "alice@acceptance.com", "password123"));
    }

    @Test
    void postMessageShouldReturn200() throws Exception {
        String responseJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "email": "alice@acceptance.com",
                            "password": "password123"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(responseJson, "$.token");

        MockMultipartFile picture = new MockMultipartFile(
                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api/rentals")
                .file(picture)
                .param("name", "Test Rental")
                .param("surface", "100")
                .param("price", "1500")
                .param("description", "A nice rental for testing.")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Rental rental = rentalRepository.findAll().get(0);
        Long rentalId = rental.getId();
        Long userId = rental.getOwner().getId();

        mockMvc.perform(post("/api/messages")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content("""
                        {
                        "rental_id": %d,
                        "message": "Hello, is this item still available?",
                        "user_id": %d
                        }
                        """.formatted(rentalId, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message send with success !"));
    }

}
