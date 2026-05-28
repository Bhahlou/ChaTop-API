package com.chatop.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Success message response")
public class SuccessMessageResponse {

    @Schema(description = "Success message", example = "Operation completed successfully")
    private String message;

}
