package com.chatop.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Rental details response")
public class RentalResponse {

    @Schema(description = "Rental ID", example = "1")
    private Long id;

    @Schema(description = "Rental name", example = "My Rental")
    private String name;

    @Schema(description = "Rental surface", example = "100")
    private Integer surface;

    @Schema(description = "Rental price", example = "1000")
    private Integer price;

    @Schema(description = "Rental picture URL", example = "https://example.com/picture.jpg")
    private String picture;

    @Schema(description = "Rental description", example = "A beautiful rental property")
    private String description;

    @JsonProperty("owner_id")
    @Schema(description = "Rental owner id", example = "1")
    private Long ownerId;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @Schema(description = "Rental creation timestamp", example = "2022/02/02")
    private String createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @Schema(description = "Rental last update timestamp", example = "2022/02/02")
    private String updatedAt;
}
