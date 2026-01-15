package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

        // ✅ last message for conversation list
        Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

        // ✅ count unseen messages (for badge)
        long countByConversationIdAndReceiverIdAndSeenFalse(Long conversationId, Long receiverId);

        // ✅ mark seen (receiver opens chat)
        @Modifying
        @Query("""
                            update Message m
                            set m.seen = true, m.seenAt = :now
                            where m.conversationId = :conversationId
                              and m.receiverId = :myId
                              and m.seen = false
                        """)
        int markSeen(@Param("conversationId") Long conversationId,
                        @Param("myId") Long myId,
                        @Param("now") LocalDateTime now);

}
