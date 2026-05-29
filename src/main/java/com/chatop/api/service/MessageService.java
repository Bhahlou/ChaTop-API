package com.chatop.api.service;

import com.chatop.api.dto.MessageRequest;
import com.chatop.api.dto.SuccessMessageResponse;

public interface MessageService {

    /*
     * Sends a message.
     * 
     * @param request the message request
     * 
     * @param userEmail the email of the user sending the message
     * 
     * @return the response containing the result of the operation
     */
    SuccessMessageResponse sendMessage(MessageRequest request, String userEmail);
}
