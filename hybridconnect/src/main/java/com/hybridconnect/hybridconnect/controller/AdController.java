package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.dto.AdCreateRequest;
import com.hybridconnect.hybridconnect.dto.PublicAdDto;
import com.hybridconnect.hybridconnect.entity.Ad;
import com.hybridconnect.hybridconnect.repository.AdRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hybridconnect.hybridconnect.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // Create Ad (for now public; later require login/JWT)
    @PostMapping("/create")
    public String create(HttpServletRequest request, @RequestBody AdCreateRequest req) {

        Long userId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (userId == null)
            return "Missing token";

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty())
            return "User not found";

        Ad ad = new Ad();
        ad.setUser(userOpt.get());
        ad.setAdType(req.adType);
        ad.setTitle(req.title);
        ad.setDescription(req.description);
        ad.setLocation(req.location);
        ad.setPriceRange(req.priceRange);

        ad.setStatus("PENDING");
        adRepository.save(ad);

        return "Ad created (Pending approval)";
    }

    // Public list of APPROVED ads
    @GetMapping("/public/list")
    public List<PublicAdDto> list() {

        return adRepository.findByStatusOrderByCreatedAtDesc("APPROVED")
                .stream()
                .map(a -> new PublicAdDto(
                        a.getId(),
                        a.getUser().getId(),
                        a.getUser().getName(),
                        a.getAdType(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getLocation(),
                        a.getPriceRange(),
                        a.getFeatured(),
                        a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Admin: view pending ads
    @GetMapping("/admin/pending")
    public List<PublicAdDto> pending() {
        return adRepository.findByStatusOrderByCreatedAtDesc("PENDING")
                .stream()
                .map(a -> new PublicAdDto(
                        a.getId(),
                        a.getUser().getId(),
                        a.getUser().getName(),
                        a.getAdType(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getLocation(),
                        a.getPriceRange(),
                        a.getFeatured(),
                        a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Admin: approve ad
    @PutMapping("/admin/approve/{adId}")
    public String approve(@PathVariable Long adId) {
        var adOpt = adRepository.findById(adId);
        if (adOpt.isEmpty())
            return "Ad not found";

        Ad ad = adOpt.get();
        ad.setStatus("APPROVED");
        adRepository.save(ad);

        return "Ad approved";
    }

    // Admin: reject ad
    @PutMapping("/admin/reject/{adId}")
    public String reject(@PathVariable Long adId) {
        var adOpt = adRepository.findById(adId);
        if (adOpt.isEmpty())
            return "Ad not found";

        Ad ad = adOpt.get();
        ad.setStatus("REJECTED");
        adRepository.save(ad);

        return "Ad rejected";
    }

}
