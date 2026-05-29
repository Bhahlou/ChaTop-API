package com.chatop.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Message request payload")
@Data
@AllArgsConstructor
public class MessageRequest {

    @Schema(description = "Rental ID", example = "12345")
    @Positive(message = "Rental ID must be a positive number")
    @JsonProperty("rental_id")
    private Long rentalId;

    @Schema(description = "User ID", example = "67890")
    @Positive(message = "User ID must be a positive number")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Message content", example = "Hello, I have a question about my rental.")
    @JsonProperty("message")
    private String message;
}
