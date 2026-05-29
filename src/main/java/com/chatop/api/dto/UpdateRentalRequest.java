package com.chatop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Schema(description = "Update rental request payload")
@Data
public class UpdateRentalRequest {

    @Schema(description = "Name of the rental", example = "Cozy Apartment")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Surface area of the rental", example = "50")
    @Positive(message = "Surface must be positive")
    private Integer surface;

    @Schema(description = "Price per night of the rental", example = "100")
    @Positive(message = "Price must be positive")
    private Integer price;

    @Schema(description = "Description of the rental", example = "A cozy apartment in the heart of the city")
    @NotBlank(message = "Description is required")
    private String description;

}
