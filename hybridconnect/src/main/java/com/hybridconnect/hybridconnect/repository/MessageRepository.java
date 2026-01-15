package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Query("""
                select m from Message m
                where (m.senderId = :a and m.receiverId = :b)
                   or (m.senderId = :b and m.receiverId = :a)
                order by m.createdAt asc
            """)
    List<Message> findChatBetween(@Param("a") Long a, @Param("b") Long b);

    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

}
