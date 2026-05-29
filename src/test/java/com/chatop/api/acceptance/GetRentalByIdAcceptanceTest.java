package com.chatop.api.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.nio.file.Path;
import java.util.List;

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
import com.chatop.api.service.AuthService;
import com.jayway.jsonpath.JsonPath;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GetRentalByIdAcceptanceTest {

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
    private Long rentalId;

    @BeforeEach
    void setUp() throws Exception {
        authService.register(new RegisterRequest("Alice", "alice@acceptance.com", "password123"));
        token = loginAs("alice@acceptance.com", "password123");
        rentalId = createRentalAndGetId("Beach House");

    }

    @Test
    void shouldReturnRentalById() throws Exception {
        mockMvc.perform(get("/api/rentals/{id}", rentalId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beach House"))
                .andExpect(jsonPath("$.surface").value(80))
                .andExpect(jsonPath("$.price").value(1200))
                .andExpect(jsonPath("$.description").value("A lovely place."))
                .andExpect(jsonPath("$.picture").exists());
    }

    @Test
    void shouldReturnNotFoundForNonExistingRental() throws Exception {
        mockMvc.perform(get("/api/rentals/{id}", 9999L)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/rentals/{id}", rentalId))
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

    private Long createRentalAndGetId(String name) throws Exception {
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

        var response = mockMvc.perform(get("/api/rentals")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Integer> ids = JsonPath.read(response, "$.rentals[?(@.name == '" + name + "')].id");
        return ids.get(0).longValue();
    }

}
