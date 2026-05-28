package com.chatop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Create rental request payload")
@Data
public class CreateRentalRequest {

    @Schema(description = "Name of the rental", example = "Cozy Apartment")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Surface area of the rental", example = "50")
    @Positive(message = "Surface must be positive")
    private long surface;

    @Schema(description = "Price per night of the rental", example = "100")
    @Positive(message = "Price must be positive")
    private long price;

    @Schema(description = "Picture file of the rental")
    @NotNull(message = "Picture is required")
    private MultipartFile picture;

    @Schema(description = "Description of the rental", example = "A cozy apartment in the heart of the city")
    @NotBlank(message = "Description is required")
    private String description;

}
