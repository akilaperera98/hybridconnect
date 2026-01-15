package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    List<Conversation> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    @Query("select c from Conversation c where c.user1Id = :myId or c.user2Id = :myId order by c.createdAt desc")
    List<Conversation> findMyConversations(@Param("myId") Long myId);

}
