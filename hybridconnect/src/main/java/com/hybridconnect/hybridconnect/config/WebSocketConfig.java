package com.hybridconnect.hybridconnect.config;

import com.hybridconnect.hybridconnect.security.WebSocketJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    public WebSocketConfig(WebSocketJwtInterceptor webSocketJwtInterceptor) {
        this.webSocketJwtInterceptor = webSocketJwtInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");

        // ✅ simple broker for /queue
        config.enableSimpleBroker("/queue");

        // ✅ user destination prefix for /user/queue/...
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtInterceptor);
    }
}
