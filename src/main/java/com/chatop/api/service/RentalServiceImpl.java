package com.chatop.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.entity.Rental;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.repository.UserRepository;

@Service
public class RentalServiceImpl implements RentalService {

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

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(filename));
            return baseUrl + "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

}
