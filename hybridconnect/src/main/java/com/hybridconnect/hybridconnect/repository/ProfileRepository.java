package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    long count();

}
