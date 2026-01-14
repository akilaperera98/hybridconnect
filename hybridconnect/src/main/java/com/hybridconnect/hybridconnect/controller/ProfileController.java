package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.entity.Profile;
import com.hybridconnect.hybridconnect.entity.ProfilePhoto;
import com.hybridconnect.hybridconnect.repository.ProfilePhotoRepository;
import com.hybridconnect.hybridconnect.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.hybridconnect.hybridconnect.dto.ProfileUpdateRequest;
import com.hybridconnect.hybridconnect.entity.User;
import com.hybridconnect.hybridconnect.repository.UserRepository;

import com.hybridconnect.hybridconnect.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;

import com.hybridconnect.hybridconnect.dto.PublicProfileDto;
import java.util.stream.Collectors;

import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfilePhotoRepository profilePhotoRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtService jwtService;

    // Public profiles list (Main page)
    @GetMapping("/public/list")
    public List<PublicProfileDto> publicList() {

        List<Profile> profiles = profileRepository.findAll();

        return profiles.stream().map(p -> {

            User u = p.getUser();

            String primaryUrl = profilePhotoRepository
                    .findFirstByUser_IdAndIsPrimaryTrue(u.getId())
                    .map(ph -> "/uploads/" + ph.getFileName())
                    .orElse(null);

            PublicProfileDto dto = new PublicProfileDto();
            dto.setUserId(u.getId());
            dto.setName(u.getName());
            dto.setRole(u.getRole());
            dto.setProfileType(p.getProfileType());
            dto.setBio(p.getBio());
            dto.setLocation(p.getLocation());
            dto.setAge(p.getAge());
            dto.setVerified(p.getVerified()); // (ඔබේ getter එක තියෙන්නේ මෙහෙම නම්)
            dto.setPrimaryPhotoUrl(primaryUrl); // ✅ FIXED

            return dto;

        }).toList();
    }

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/update/{userId}")
    public String updateProfile(@PathVariable Long userId,
            @RequestBody ProfileUpdateRequest req) {

        var profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty())
            return "Profile not found";

        Profile profile = profileOpt.get();

        profile.setProfileType(req.profileType);
        profile.setBio(req.bio);
        profile.setLocation(req.location);
        profile.setAge(req.age);

        profileRepository.save(profile);

        return "Profile updated";
    }

    @PutMapping("/me/update")
    public String updateMyProfile(HttpServletRequest request,
            @RequestBody ProfileUpdateRequest req) {

        Long userId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (userId == null)
            return "Missing token";

        var profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty())
            return "Profile not found";

        Profile profile = profileOpt.get();

        profile.setProfileType(req.profileType);
        profile.setBio(req.bio);
        profile.setLocation(req.location);
        profile.setAge(req.age);

        profileRepository.save(profile);

        return "Profile updated";
    }

}
