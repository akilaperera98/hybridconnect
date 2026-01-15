package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.dto.WsSendMessageRequest;
import com.hybridconnect.hybridconnect.entity.Conversation;
import com.hybridconnect.hybridconnect.entity.Message;
import com.hybridconnect.hybridconnect.repository.ContactRequestRepository;
import com.hybridconnect.hybridconnect.repository.ConversationRepository;
import com.hybridconnect.hybridconnect.repository.MessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class RealtimeChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContactRequestRepository contactRequestRepository;

    public RealtimeChatController(
            SimpMessagingTemplate messagingTemplate,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ContactRequestRepository contactRequestRepository) {

        this.messagingTemplate = messagingTemplate;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.contactRequestRepository = contactRequestRepository;
    }

    // client sends to: /app/chat.send
    @MessageMapping("/chat.send")
    public void send(WsSendMessageRequest req, Principal principal) {

        // ✅ must have principal (set by WebSocketJwtInterceptor)
        if (principal == null || principal.getName() == null)
            return;

        Long myId;
        try {
            myId = Long.parseLong(principal.getName()); // principal name == "1" / "2"
        } catch (Exception e) {
            return;
        }

        if (req == null || req.toUserId == null)
            return;

        Long otherId = req.toUserId;

        if (req.text == null || req.text.trim().isEmpty())
            return;

        // ✅ allow only if accepted
        boolean accepted = contactRequestRepository.existsAcceptedBetweenUsers(myId, otherId);
        if (!accepted)
            return;

        // ✅ find or create conversation (store smaller id first)
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

        Message saved = messageRepository.save(m);

        // ✅ receiver subscribes: /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                otherId.toString(),
                "/queue/messages",
                saved);

        // ✅ optional: push to sender also
        messagingTemplate.convertAndSendToUser(
                myId.toString(),
                "/queue/messages",
                saved);
    }
}
