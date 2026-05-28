package com.chatop.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.entity.Rental;
import com.chatop.api.entity.User;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rentalService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(rentalService, "baseUrl", "http://localhost:3001");
    }

    private CreateRentalRequest buildRequest(MultipartFile picture) {
        CreateRentalRequest request = new CreateRentalRequest();
        request.setName("Beach House");
        request.setSurface(80);
        request.setPrice(1200);
        request.setDescription("Nice place by the sea");
        request.setPicture(picture);
        return request;
    }

    // -------------------------------------------------------------------------
    // createRental
    // -------------------------------------------------------------------------

    @Test
    @Tag("createRental")
    @DisplayName("createRental - Success: saves rental and returns success message")
    void createRentalShouldSaveRentalAndReturnSuccessMessage() {
        User user = new User();
        user.setEmail("alice@test.com");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(any(), eq(Rental.class))).thenReturn(new Rental());

        MockMultipartFile file = new MockMultipartFile(
                "picture", "house.jpg", "image/jpeg", "image-bytes".getBytes());

        SuccessMessageResponse response = rentalService.createRental(buildRequest(file), "alice@test.com");

        assertThat(response.getMessage()).isEqualTo("Rental created !");
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    @Tag("createRental")
    @DisplayName("createRental - User not found: throws IllegalArgumentException before saving")
    void createRentalShouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "picture", "house.jpg", "image/jpeg", "image-bytes".getBytes());

        CreateRentalRequest request = buildRequest(file);
        assertThatThrownBy(() -> rentalService.createRental(request, "unknown@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(rentalRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // saveFile (exercised via createRental)
    // -------------------------------------------------------------------------

    @Test
    @Tag("saveFile")
    @DisplayName("saveFile - Stores file in upload dir and returns URL with correct base")
    void saveFileShouldStoreFileAndReturnCorrectUrl() throws Exception {
        Rental rental = new Rental();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(modelMapper.map(any(), eq(Rental.class))).thenReturn(rental);

        MockMultipartFile file = new MockMultipartFile(
                "picture", "house.jpg", "image/jpeg", "image-bytes".getBytes());

        rentalService.createRental(buildRequest(file), "alice@test.com");

        assertThat(rental.getPicture()).startsWith("http://localhost:3001/uploads/");
        assertThat(Files.list(tempDir).count()).isEqualTo(1);
    }

    @Test
    @Tag("saveFile")
    @DisplayName("saveFile - Path traversal in filename: file stays inside upload dir")
    void saveFileShouldNotEscapeUploadDirWhenFilenameContainsPathTraversal() {
        Rental rental = new Rental();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(modelMapper.map(any(), eq(Rental.class))).thenReturn(rental);

        MockMultipartFile maliciousFile = new MockMultipartFile(
                "picture", "../../evil.sh", "image/jpeg", "payload".getBytes());

        rentalService.createRental(buildRequest(maliciousFile), "alice@test.com");

        String storedFilename = rental.getPicture().substring(rental.getPicture().lastIndexOf('/') + 1);
        Path storedPath = tempDir.resolve(storedFilename).normalize();
        assertThat(storedPath).startsWith(tempDir);
        assertThat(Files.exists(storedPath)).isTrue();
    }

    @Test
    @Tag("saveFile")
    @DisplayName("saveFile - IO error: wraps IOException in RuntimeException")
    void saveFileShouldThrowRuntimeExceptionOnIOError() throws Exception {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        MultipartFile brokenFile = mock(MultipartFile.class);
        when(brokenFile.getOriginalFilename()).thenReturn("house.jpg");
        when(brokenFile.getInputStream()).thenThrow(new IOException("disk full"));

        CreateRentalRequest brokenRequest = buildRequest(brokenFile);
        assertThatThrownBy(() -> rentalService.createRental(brokenRequest, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to store file");

        verify(rentalRepository, never()).save(any());
    }
}
