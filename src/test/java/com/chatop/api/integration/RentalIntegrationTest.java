package com.chatop.api.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
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
import com.chatop.api.security.JwtAuthenticationFilter;
import com.chatop.api.service.AuthService;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.FilterChain;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RentalIntegrationTest {

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
    void createRentalShouldReturn200WithSuccessMessage() throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api/rentals")
                .file(picture)
                .param("name", "Test Rental")
                .param("surface", "100")
                .param("price", "1500")
                .param("description", "A nice rental for testing."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rental created !"));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("getAll")
    void getRentalsShouldReturn200WithPersistedRentals() throws Exception {
        createRental("Beach House");
        createRental("Mountain Cabin");

        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentals", hasSize(2)))
                .andExpect(jsonPath("$.rentals[0].name").value("Beach House"))
                .andExpect(jsonPath("$.rentals[1].name").value("Mountain Cabin"));
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    @Tag("getById")
    void getRentalByIdShouldReturn200WithRentalDetails() throws Exception {
        Long rentalId = createRentalAndGetId("Beach House");

        mockMvc.perform(get("/api/rentals/{id}", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beach House"))
                .andExpect(jsonPath("$.surface").value(80))
                .andExpect(jsonPath("$.price").value(1200))
                .andExpect(jsonPath("$.description").value("A lovely place."))
                .andExpect(jsonPath("$.picture").exists());
    }

    private void createRental(String name) throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "rental.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());
        mockMvc.perform(multipart("/api/rentals")
                .file(picture)
                .param("name", name)
                .param("surface", "80")
                .param("price", "1200")
                .param("description", "A lovely place."))
                .andExpect(status().isOk());
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
