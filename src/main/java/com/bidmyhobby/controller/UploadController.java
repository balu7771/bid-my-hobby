package com.bidmyhobby.controller;

import com.bidmyhobby.service.ModerationService;
import com.bidmyhobby.service.ScalityStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private ScalityStorageService scalityStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image,
                                         @RequestParam("title") String title,
                                         @RequestParam("description") String description) {
        try {
            //boolean isSafe = moderationService.moderateText(title + "\n" + description);
            //if (!isSafe) return ResponseEntity.badRequest().body("Content not allowed.");

            String imageUrl = scalityStorageService.uploadFile(image);
            return ResponseEntity.ok().body("Uploaded successfully: " + imageUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}
