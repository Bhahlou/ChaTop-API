package com.chatop.api.service;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.SuccessMessageResponse;

public interface RentalService {
    /**
     * Creates a new rental based on the provided request.
     *
     * @param request   The request containing rental details.
     * @param userEmail The email of the user creating the rental.
     * @return A response indicating the success of the operation.
     */
    SuccessMessageResponse createRental(CreateRentalRequest request, String userEmail);

    /**
     * Retrieves a list of all rentals.
     * 
     * @return A list of all rentals.
     */
    GetAllRentalsResponse getRentals();
}
