package com.hybridconnect.hybridconnect.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketJwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null)
            return message;

        // ✅ Only handle CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // STOMP connect header "Authorization"
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null)
                authHeader = accessor.getFirstNativeHeader("authorization");

            System.out.println("WS CONNECT Authorization: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Long userId = jwtService.getUserIdFromToken(token);

                // ✅ IMPORTANT: principal must be STRING username "2", "1" ...
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                accessor.setUser(authentication);

                System.out.println("WS PRINCIPAL SET TO: " + userId);
            }
        }

        return message;
    }
}
