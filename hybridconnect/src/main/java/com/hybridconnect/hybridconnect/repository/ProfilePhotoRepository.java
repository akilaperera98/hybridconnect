package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {

    List<ProfilePhoto> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<ProfilePhoto> findByUser_Id(Long userId);

    Optional<ProfilePhoto> findFirstByUser_IdAndIsPrimaryTrue(Long userId); // ✅ FIXED
}
