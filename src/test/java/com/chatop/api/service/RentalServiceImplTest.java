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
import java.time.LocalDateTime;
import java.util.List;
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
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.RentalResponse;
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

    // -------------------------------------------------------------------------
    // getRentals
    // -------------------------------------------------------------------------

    @Test
    @Tag("getRentals")
    @DisplayName("getRentals - Returns all rentals mapped to RentalResponse")
    void getRentalsShouldReturnMappedRentals() {
        User owner = new User();
        owner.setId(1L);

        Rental rental1 = new Rental();
        rental1.setId(1L);
        rental1.setName("Beach House");
        rental1.setSurface(80);
        rental1.setPrice(1200);
        rental1.setPicture("http://example.com/pic1.jpg");
        rental1.setDescription("Nice place");
        rental1.setOwner(owner);
        rental1.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        rental1.setUpdatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));

        Rental rental2 = new Rental();
        rental2.setId(2L);
        rental2.setName("Mountain Cabin");
        rental2.setSurface(60);
        rental2.setPrice(800);
        rental2.setPicture("http://example.com/pic2.jpg");
        rental2.setDescription("Cozy cabin");
        rental2.setOwner(owner);
        rental2.setCreatedAt(LocalDateTime.of(2022, 6, 15, 0, 0));
        rental2.setUpdatedAt(LocalDateTime.of(2022, 6, 15, 0, 0));

        when(rentalRepository.findAll()).thenReturn(List.of(rental1, rental2));

        GetAllRentalsResponse result = rentalService.getRentals();

        assertThat(result.getRentals()).hasSize(2);
        assertThat(result.getRentals().get(0).getName()).isEqualTo("Beach House");
        assertThat(result.getRentals().get(0).getOwnerId()).isEqualTo(1L);
        assertThat(result.getRentals().get(0).getCreatedAt()).isEqualTo("2022/01/01");
        assertThat(result.getRentals().get(1).getName()).isEqualTo("Mountain Cabin");
        assertThat(result.getRentals().get(1).getCreatedAt()).isEqualTo("2022/06/15");
    }

    @Test
    @Tag("getRentals")
    @DisplayName("getRentals - Empty repository returns empty list")
    void getRentalsShouldReturnEmptyListWhenNoRentals() {
        when(rentalRepository.findAll()).thenReturn(List.of());

        assertThat(rentalService.getRentals()).isNotNull()
                .returns(List.of(), GetAllRentalsResponse::getRentals);
    }

    @Test
    @Tag("getRentals")
    @DisplayName("getRentals - Repository exception propagates")
    void getRentalsShouldPropagateRepositoryException() {
        when(rentalRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> rentalService.getRentals())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    @Tag("getRentalById")
    @DisplayName("getRentalById - Success: returns rental details when found")
    void getRentalByIdShouldReturnRentalDetailsWhenFound() {
        User owner = new User();
        owner.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setName("Beach House");
        rental.setSurface(80);
        rental.setPrice(1200);
        rental.setPicture("http://example.com/pic.jpg");
        rental.setDescription("Nice place");
        rental.setOwner(owner);
        rental.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        rental.setUpdatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        RentalResponse result = rentalService.getRentalById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Beach House");
        assertThat(result.getSurface()).isEqualTo(80);
        assertThat(result.getPrice()).isEqualTo(1200);
        assertThat(result.getOwnerId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo("2022/01/01");
    }
}
