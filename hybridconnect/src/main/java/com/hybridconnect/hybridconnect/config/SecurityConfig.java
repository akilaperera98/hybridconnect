package com.hybridconnect.hybridconnect.config;

import com.hybridconnect.hybridconnect.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // ✅ allow static test page + static files
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api/profiles/public/**",
                                                                "/api/ads/public/**",
                                                                "/uploads/**",
                                                                "/api/chat/**",
                                                                "/ws/**",
                                                                "/static/**",
                                                                "/ws-test.html",
                                                                "/favicon.ico",
                                                                "/css/**",
                                                                "/js/**",

                                                                "/test")
                                                .permitAll()
                                                // ✅ allow websocket handshake endpoints (ඔයාගේ config අනුව)
                                                .requestMatchers("/ws/**", "/sockjs/**").permitAll()

                                                .anyRequest().authenticated())
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable());

                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
