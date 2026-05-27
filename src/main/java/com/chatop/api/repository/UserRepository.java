package com.chatop.api.repository;

import com.chatop.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data génère le SQL tout seul à partir du nom de la méthode
    Optional<User> findByEmail(String email);
}
