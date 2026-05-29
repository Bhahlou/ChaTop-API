package com.chatop.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response for getting all rentals")
public class GetAllRentalsResponse {

    @Schema(description = "List of rentals")
    private List<RentalResponse> rentals;
}
