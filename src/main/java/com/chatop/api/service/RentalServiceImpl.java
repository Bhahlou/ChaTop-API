package com.chatop.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.RentalResponse;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.entity.Rental;
import com.chatop.api.exception.CouldNotSSaveFile;
import com.chatop.api.exception.RentalNotFoundException;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.repository.UserRepository;

@Service
public class RentalServiceImpl implements RentalService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:3001}")
    private String baseUrl;

    public RentalServiceImpl(RentalRepository rentalRepository, UserRepository userRepository,
            ModelMapper modelMapper) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SuccessMessageResponse createRental(CreateRentalRequest request, String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String pictureUrl = saveFile(request.getPicture());

        Rental rental = modelMapper.map(request, Rental.class);
        rental.setPicture(pictureUrl);
        rental.setCreatedAt(LocalDateTime.now());
        rental.setOwner(user);
        rentalRepository.save(rental);
        return new SuccessMessageResponse("Rental created !");
    }

    @Override
    public GetAllRentalsResponse getRentals() {
        return new GetAllRentalsResponse(rentalRepository.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    @Override
    public RentalResponse getRentalById(Long id) {
        return rentalRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RentalNotFoundException("Rental not found"));
    }

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : "";
            // Use only a UUID to avoid path traversal from user-controlled filenames
            String safeFilename = UUID.randomUUID() + extension.replaceAll("[^a-zA-Z0-9.]", "");
            Path targetPath = uploadPath.resolve(safeFilename).normalize();
            if (!targetPath.startsWith(uploadPath)) {
                throw new CouldNotSSaveFile("Invalid file path");
            }
            Files.copy(file.getInputStream(), targetPath);
            return baseUrl + "/uploads/" + safeFilename;
        } catch (IOException e) {
            throw new CouldNotSSaveFile("Failed to store file");
        }
    }

    private RentalResponse toResponse(Rental rental) {
        Long ownerId = rental.getOwner() != null ? rental.getOwner().getId() : null;
        String createdAt = rental.getCreatedAt() != null ? rental.getCreatedAt().format(DATE_FORMATTER) : null;
        String updatedAt = rental.getUpdatedAt() != null ? rental.getUpdatedAt().format(DATE_FORMATTER) : null;
        return new RentalResponse(
                rental.getId(), rental.getName(), rental.getSurface(), rental.getPrice(),
                rental.getPicture(), rental.getDescription(), ownerId, createdAt, updatedAt);
    }

}
