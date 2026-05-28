package com.chatop.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Get current user information response")
public record GetMeResponse(
        @Schema(description = "User ID", example = "1") Long id,
        @Schema(description = "User name", example = "John Doe") String name,
        @Schema(description = "User email", example = "john@test.com") String email,
        @Schema(description = "User creation timestamp", example = "2022/02/02") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd") LocalDateTime created_at,
        @Schema(description = "User last update timestamp", example = "2022/02/02") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd") LocalDateTime updated_at) {

}
