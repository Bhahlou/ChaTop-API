package com.chatop.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.service.RentalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Rentals", description = "Rentals management")
@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @Operation(summary = "Create a new rental")
    @ApiResponse(responseCode = "200", description = "Rental created successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessMessageResponse> createRental(
            @Valid @ModelAttribute CreateRentalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        final SuccessMessageResponse response = rentalService.createRental(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all rentals")
    @ApiResponse(responseCode = "200", description = "List of rentals retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping
    public ResponseEntity<GetAllRentalsResponse> getAllRentals() {
        final GetAllRentalsResponse response = rentalService.getRentals();
        return ResponseEntity.ok(response);
    }
}
