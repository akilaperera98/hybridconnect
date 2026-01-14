package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {

    List<ContactRequest> findByToProfile_User_Id(Long userId);

    boolean existsByFromUser_IdAndToProfile_Id(Long fromUserId, Long toProfileId);

}
