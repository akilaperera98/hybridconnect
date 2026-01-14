package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.dto.ContactRequestCreateDto;
import com.hybridconnect.hybridconnect.entity.ContactRequest;
import com.hybridconnect.hybridconnect.repository.ContactRequestRepository;
import com.hybridconnect.hybridconnect.repository.ProfileRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hybridconnect.hybridconnect.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/contacts")
public class ContactRequestController {

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtService jwtService;

    // Send request (female → male profile)
    @PostMapping("/request")
    public String send(HttpServletRequest request, @RequestBody ContactRequestCreateDto req) {

        Long fromUserId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (fromUserId == null)
            return "Missing token";

        if (contactRequestRepository.existsByFromUser_IdAndToProfile_Id(fromUserId, req.toProfileId)) {
            return "Request already sent";
        }

        var fromUserOpt = userRepository.findById(fromUserId);
        var profileOpt = profileRepository.findById(req.toProfileId);

        if (fromUserOpt.isEmpty() || profileOpt.isEmpty())
            return "Invalid user/profile";

        ContactRequest cr = new ContactRequest();
        cr.setFromUser(fromUserOpt.get());
        cr.setToProfile(profileOpt.get());

        contactRequestRepository.save(cr);

        return "Contact request sent";
    }

    // Male view incoming requests
    @GetMapping("/incoming/{userId}")
    public java.util.List<ContactRequest> incoming(@PathVariable Long userId) {
        return contactRequestRepository.findByToProfile_User_Id(userId);
    }

    // Accept
    @PutMapping("/accept/{id}")
    public String accept(@PathVariable Long id) {
        var crOpt = contactRequestRepository.findById(id);
        if (crOpt.isEmpty())
            return "Not found";

        ContactRequest cr = crOpt.get();
        cr.setStatus("ACCEPTED");
        contactRequestRepository.save(cr);

        return "Accepted";
    }

    // Reject
    @PutMapping("/reject/{id}")
    public String reject(@PathVariable Long id) {
        var crOpt = contactRequestRepository.findById(id);
        if (crOpt.isEmpty())
            return "Not found";

        ContactRequest cr = crOpt.get();
        cr.setStatus("REJECTED");
        contactRequestRepository.save(cr);

        return "Rejected";
    }
}
