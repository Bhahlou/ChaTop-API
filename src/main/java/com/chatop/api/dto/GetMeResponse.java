package com.chatop.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Get current user information response")
public class GetMeResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User email", example = "john@test.com")
    private String email;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @Schema(description = "User creation timestamp", example = "2022/02/02")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    @Schema(description = "User last update timestamp", example = "2022/02/02")
    private LocalDateTime updatedAt;
}
