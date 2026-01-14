package com.hybridconnect.hybridconnect.controller;

import com.hybridconnect.hybridconnect.entity.ProfilePhoto;
import com.hybridconnect.hybridconnect.repository.ProfilePhotoRepository;
import com.hybridconnect.hybridconnect.repository.UserRepository;
import com.hybridconnect.hybridconnect.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hybridconnect.hybridconnect.dto.PhotoDto;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfilePhotoRepository profilePhotoRepository;

    @PostMapping("/upload")
    public String upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws Exception {

        Long userId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (userId == null)
            return "Missing token";

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty())
            return "User not found";

        if (file.isEmpty())
            return "No file selected";

        // basic safety: only images
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "Only image files allowed";
        }

        // ensure uploads folder exists
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        // unique filename
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID() + ext;

        Path savePath = Paths.get(uploadDir, savedName);
        Files.write(savePath, file.getBytes());

        ProfilePhoto photo = new ProfilePhoto();
        photo.setUser(userOpt.get());
        photo.setFileName(savedName);
        photo.setFilePath(uploadDir + "/" + savedName);

        profilePhotoRepository.save(photo);

        return "Uploaded: /uploads/" + savedName;
    }

    @GetMapping("/my/list")
    public List<PhotoDto> myPhotos(HttpServletRequest request) {

        Long userId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (userId == null)
            return List.of();

        return profilePhotoRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(p -> new PhotoDto(
                        p.getId(),
                        "/uploads/" + p.getFileName(),
                        p.getIsPrimary(),
                        p.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @PutMapping("/set-primary/{photoId}")
    public String setPrimary(HttpServletRequest request, @PathVariable Long photoId) {

        Long userId = jwtService.extractUserIdFromAuthHeader(request.getHeader("Authorization"));
        if (userId == null)
            return "Missing token";

        var photoOpt = profilePhotoRepository.findById(photoId);
        if (photoOpt.isEmpty())
            return "Photo not found";

        ProfilePhoto target = photoOpt.get();

        // ownership check
        if (!target.getUser().getId().equals(userId)) {
            return "Not allowed";
        }

        // set all false
        List<ProfilePhoto> all = profilePhotoRepository.findByUser_Id(userId);
        for (ProfilePhoto p : all) {
            p.setIsPrimary(false);
        }

        // set selected true
        target.setIsPrimary(true);

        profilePhotoRepository.saveAll(all);
        profilePhotoRepository.save(target);

        return "Primary photo updated";
    }

}
