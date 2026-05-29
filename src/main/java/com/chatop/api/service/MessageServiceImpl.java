package com.chatop.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.chatop.api.dto.MessageRequest;
import com.chatop.api.dto.SuccessMessageResponse;
import com.chatop.api.entity.Message;
import com.chatop.api.exception.RentalNotFoundException;
import com.chatop.api.exception.UserNotFoundException;
import com.chatop.api.repository.MessageRepository;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.repository.UserRepository;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository,
            RentalRepository rentalRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    public SuccessMessageResponse sendMessage(MessageRequest request, String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        var rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new RentalNotFoundException("Rental not found"));

        Message message = new Message();
        message.setContent(request.getMessage());
        message.setUser(user);
        message.setRental(rental);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);

        return new SuccessMessageResponse("Message send with success !");
    }
}
