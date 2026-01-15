package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {

    List<ContactRequest> findByToProfile_User_Id(Long userId);

    boolean existsByFromUser_IdAndToProfile_Id(Long fromUserId, Long toProfileId);

    @Query("""
                select count(cr) > 0
                from ContactRequest cr
                where
                  (
                    (cr.fromUser.id = :a and cr.toProfile.user.id = :b)
                    or
                    (cr.fromUser.id = :b and cr.toProfile.user.id = :a)
                  )
                  and cr.status = 'ACCEPTED'
            """)
    boolean existsAcceptedBetweenUsers(@Param("a") Long a, @Param("b") Long b);

}
