package com.chatop.api.service;

import com.chatop.api.dto.GetUserResponse;

public interface UserService {

    /*
     * 
     * Retrieves a user by their ID and maps them to a GetUserResponse.
     * 
     * @param id the ID of the user to retrieve
     * 
     * @return the GetUserResponse containing the user's information
     */
    GetUserResponse getUserById(Long id);
}
