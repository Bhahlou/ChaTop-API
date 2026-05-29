package com.chatop.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.chatop.api.dto.MessageRequest;
import com.chatop.api.entity.Message;
import com.chatop.api.entity.Rental;
import com.chatop.api.entity.User;
import com.chatop.api.exception.RentalNotFoundException;
import com.chatop.api.exception.UserNotFoundException;
import com.chatop.api.repository.MessageRepository;
import com.chatop.api.repository.RentalRepository;
import com.chatop.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    @Tag("sendMessage")
    @DisplayName("sendMessage - Success: saves message and returns success response")
    void sendMessageShouldSaveMessageAndReturnSuccessResponse() {
        User user = new User();
        user.setEmail("alice@test.com");

        Rental rental = new Rental();
        rental.setId(1L);

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        var response = messageService.sendMessage(new MessageRequest(1L, 1L, "Hello!"), "alice@test.com");

        assertThat(response.getMessage()).isEqualTo("Message send with success !");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @Tag("sendMessage")
    @DisplayName("sendMessage - User not found: throws UserNotFoundException before saving")
    void sendMessageShouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        MessageRequest request = new MessageRequest(1L, 1L, "Hello!");
        assertThatThrownBy(() -> messageService.sendMessage(request, "unknown@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @Tag("sendMessage")
    @DisplayName("sendMessage - Rental not found: throws RentalNotFoundException before saving")
    void sendMessageShouldThrowWhenRentalNotFound() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(new User()));
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        MessageRequest request = new MessageRequest(99L, 1L, "Hello!");
        assertThatThrownBy(() -> messageService.sendMessage(request, "alice@test.com"))
                .isInstanceOf(RentalNotFoundException.class)
                .hasMessage("Rental not found");

        verify(messageRepository, never()).save(any());
    }
}
