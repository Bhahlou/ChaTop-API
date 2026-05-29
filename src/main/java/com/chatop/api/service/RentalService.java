package com.chatop.api.service;

import com.chatop.api.dto.CreateRentalRequest;
import com.chatop.api.dto.GetAllRentalsResponse;
import com.chatop.api.dto.RentalResponse;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.dto.UpdateRentalRequest;

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

    /**
     * Retrieves a rental by its ID.
     *
     * @param id The ID of the rental to retrieve.
     * @return The rental details if found.
     */
    RentalResponse getRentalById(Long id);

    /**
     * Updates an existing rental with the provided details.
     *
     * @param id      The ID of the rental to update.
     * @param request The request containing updated rental details.
     * @return A response indicating the success of the operation.
     */
    SuccessMessageResponse updateRental(Long id, UpdateRentalRequest request);
}
