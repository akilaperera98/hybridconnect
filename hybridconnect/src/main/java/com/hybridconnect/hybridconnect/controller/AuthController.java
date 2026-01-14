package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.entity.Profile;
import com.hybridconnect.hybridconnect.entity.User;
import com.hybridconnect.hybridconnect.repository.ProfileRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.hybridconnect.hybridconnect.dto.LoginRequest;
import com.hybridconnect.hybridconnect.dto.LoginResponse;

import com.hybridconnect.hybridconnect.security.JwtService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        // encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // save user
        User savedUser = userRepository.save(user);

        // create empty profile automatically
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setProfileType("HYBRID");
        profileRepository.save(profile);

        return "User registered successfully!";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {

        Optional<User> userOpt = userRepository.findByEmail(req.email);

        if (userOpt.isEmpty()) {
            return new LoginResponse(false, "Email not found", null, null, null);
        }

        User user = userOpt.get();

        // 🔴 BLOCK CHECK
        if ("BLOCKED".equals(user.getStatus())) {
            return new LoginResponse(false, "User is blocked by admin", null, null, null);
        }

        boolean ok = passwordEncoder.matches(req.password, user.getPassword());
        if (!ok) {
            return new LoginResponse(false, "Invalid password", null, null, null);
        }

        // ✅ JWT TOKEN GENERATE
        String token = jwtService.generateToken(user.getId(), user.getRole());

        return new LoginResponse(true, "Login success", user.getId(), user.getRole(), token);
    }

}
