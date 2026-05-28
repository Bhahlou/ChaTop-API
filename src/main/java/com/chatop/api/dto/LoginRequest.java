package com.chatop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "User login payload")
@Data
public class LoginRequest {

    @Schema(description = "Email address", example = "alice@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password", example = "s3cur3P@ss")
    @NotBlank(message = "Password is required")
    private String password;
}