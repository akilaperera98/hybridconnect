package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.dto.ConversationDto;
import com.hybridconnect.hybridconnect.dto.SendMessageRequest;
import com.hybridconnect.hybridconnect.entity.Conversation;
import com.hybridconnect.hybridconnect.entity.Message;
import com.hybridconnect.hybridconnect.entity.User;
import com.hybridconnect.hybridconnect.repository.ContactRequestRepository;
import com.hybridconnect.hybridconnect.repository.ConversationRepository;
import com.hybridconnect.hybridconnect.repository.MessageRepository;
import com.hybridconnect.hybridconnect.repository.ProfilePhotoRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfilePhotoRepository profilePhotoRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    // ✅ Send message (only if accepted)
    @PostMapping("/send")
    public String send(@RequestBody SendMessageRequest req) {

        Long myId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long otherId = req.toUserId;

        if (req.text == null || req.text.trim().isEmpty()) {
            return "Message empty";
        }

        boolean accepted = contactRequestRepository.existsAcceptedBetweenUsers(myId, otherId);
        if (!accepted) {
            return "Not allowed (not accepted)";
        }

        Long a = Math.min(myId, otherId);
        Long b = Math.max(myId, otherId);

        Conversation convo = conversationRepository.findByUser1IdAndUser2Id(a, b)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser1Id(a);
                    c.setUser2Id(b);
                    return conversationRepository.save(c);
                });

        Message m = new Message();
        m.setConversationId(convo.getId());
        m.setSenderId(myId);
        m.setReceiverId(otherId);
        m.setText(req.text.trim());

        messageRepository.save(m);

        return "Message sent";
    }

    // ✅ My conversations list (ConversationDto list)
    @GetMapping("/conversations")
    public List<ConversationDto> myConversations() {

        Long myId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Conversation> list = conversationRepository.findMyConversations(myId);

        return list.stream().map(c -> {

            Long otherId = c.getUser1Id().equals(myId) ? c.getUser2Id() : c.getUser1Id();

            User other = userRepository.findById(otherId).orElseThrow();

            String photoUrl = profilePhotoRepository
                    .findFirstByUser_IdAndIsPrimaryTrue(otherId)
                    .map(ph -> "/uploads/" + ph.getFileName())
                    .orElse(null);

            Message last = messageRepository
                    .findFirstByConversationIdOrderByCreatedAtDesc(c.getId())
                    .orElse(null);

            String lastText = (last == null) ? null : last.getText();
            LocalDateTime lastAt = (last == null) ? null : last.getCreatedAt();

            // ✅ unread count for ME
            Long unread = messageRepository.countByConversationIdAndReceiverIdAndSeenFalse(c.getId(), myId);

            // ✅ IMPORTANT: make sure your ConversationDto has this constructor
            return new ConversationDto(
                    otherId,
                    other.getName(),
                    photoUrl,
                    lastText,
                    lastAt,
                    unread);

        }).toList();
    }

    // ✅ Get messages by conversationId
    @GetMapping("/messages/conversation/{conversationId}")
    public List<Message> messagesByConversation(@PathVariable Long conversationId) {

        Long myId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Conversation c = conversationRepository.findById(conversationId).orElseThrow();

        if (!(c.getUser1Id().equals(myId) || c.getUser2Id().equals(myId))) {
            throw new RuntimeException("Not allowed");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    // ✅ Get messages with a user (chat thread)
    @GetMapping("/messages/with/{otherUserId}")
    public List<Message> messagesWithUser(@PathVariable Long otherUserId) {

        Long myId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean accepted = contactRequestRepository.existsAcceptedBetweenUsers(myId, otherUserId);
        if (!accepted) {
            throw new RuntimeException("Not allowed (not accepted)");
        }

        return messageRepository.findChatBetween(myId, otherUserId);
    }

    // ✅ Mark messages as seen
    @PutMapping("/messages/seen/{conversationId}")
    @Transactional
    public String markSeen(@PathVariable Long conversationId) {

        Long myId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Conversation c = conversationRepository.findById(conversationId).orElseThrow();
        if (!(c.getUser1Id().equals(myId) || c.getUser2Id().equals(myId))) {
            throw new RuntimeException("Not allowed");
        }

        int updated = messageRepository.markSeen(conversationId, myId, LocalDateTime.now());
        return "Seen updated: " + updated;
    }
}
