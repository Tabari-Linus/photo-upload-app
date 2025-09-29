package com.lii.photouploader.controller;

import com.lii.photouploader.entity.Photos;
import com.lii.photouploader.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class PhotoController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    @Autowired
    private PhotoService photoService;

    @GetMapping("/")
    public String gallery(Model model) {
        try {
            model.addAttribute("photos", photoService.getAllPhotos());
        } catch (Exception e) {
            logger.error("Error loading gallery", e);
            model.addAttribute("error", "Error loading photos: " + e.getMessage());
        }
        return "gallery";
    }

    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              @RequestParam("description") String description,
                              Model model) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Please select a file to upload.");
                return "upload";
            }

            Photos uploadedPhotos = photoService.uploadPhoto(file, description);
            logger.info("Photo uploaded successfully: {}", uploadedPhotos.getId());

            model.addAttribute("success", "Photo uploaded successfully!");

        } catch (IllegalArgumentException e) {
            logger.warn("Upload validation failed: {}", e.getMessage());
            model.addAttribute("error", "Upload failed: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during upload", e);
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "upload";
    }


    @GetMapping("/health")
    public String health() {
        return "health";
    }


}