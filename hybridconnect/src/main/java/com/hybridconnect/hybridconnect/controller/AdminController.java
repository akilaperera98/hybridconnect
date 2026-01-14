package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.repository.AdRepository;
import com.hybridconnect.hybridconnect.repository.ProfileRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hybridconnect.hybridconnect.dto.AdminUserDto;
import com.hybridconnect.hybridconnect.entity.User;

import java.util.List;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AdRepository adRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", userRepository.count());
        data.put("activeUsers", userRepository.countByStatus("ACTIVE"));
        data.put("blockedUsers", userRepository.countByStatus("BLOCKED"));

        data.put("totalProfiles", profileRepository.count());

        data.put("pendingAds", adRepository.countByStatus("PENDING"));
        data.put("approvedAds", adRepository.countByStatus("APPROVED"));
        data.put("rejectedAds", adRepository.countByStatus("REJECTED"));

        return data;
    }

    @GetMapping("/users")
    public List<AdminUserDto> users() {
        return userRepository.findAll()
                .stream()
                .map(u -> new AdminUserDto(
                        u.getId(),
                        u.getRole(),
                        u.getName(),
                        u.getEmail(),
                        u.getStatus()))
                .collect(Collectors.toList());
    }

    @PutMapping("/users/block/{userId}")
    public String blockUser(@PathVariable Long userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty())
            return "User not found";

        User user = userOpt.get();
        user.setStatus("BLOCKED");
        userRepository.save(user);

        return "User blocked";
    }

    @PutMapping("/users/unblock/{userId}")
    public String unblockUser(@PathVariable Long userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty())
            return "User not found";

        User user = userOpt.get();
        user.setStatus("ACTIVE");
        userRepository.save(user);

        return "User unblocked";
    }

}
