package com.chatop.api.acceptance;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.transaction.annotation.Transactional;

import com.chatop.api.dto.RegisterRequest;
import com.chatop.api.service.AuthService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GetRentalsAcceptanceTest {

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

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        authService.register(new RegisterRequest("Alice", "alice@acceptance.com", "password123"));
        token = loginAs("alice@acceptance.com", "password123");
        createRental("Beach House");
        createRental("Mountain Cabin");
    }

    @Test
    void getRentalsShouldReturnCreatedRentals() throws Exception {
        mockMvc.perform(get("/api/rentals")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentals").isArray())
                .andExpect(jsonPath("$.rentals", hasSize(2)))
                .andExpect(jsonPath("$.rentals[0].name").value("Beach House"))
                .andExpect(jsonPath("$.rentals[1].name").value("Mountain Cabin"));
    }

    @Test
    void getRentalsWithoutAuthShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isUnauthorized());
    }

    private String loginAs(String email, String password) throws Exception {
        String responseJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(responseJson, "$.token");
    }

    private void createRental(String name) throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());
        mockMvc.perform(multipart("/api/rentals")
                .file(picture)
                .param("name", name)
                .param("surface", "80")
                .param("price", "1200")
                .param("description", "A lovely place.")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
