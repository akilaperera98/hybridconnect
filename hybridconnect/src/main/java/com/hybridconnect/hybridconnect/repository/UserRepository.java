package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByStatus(String status);

}
