package com.lii.photouploader.controller;


import com.lii.photouploader.dto.PhotoDTO;
import com.lii.photouploader.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Web Controller for handling photo gallery operations
 * Provides endpoints for viewing and uploading photos
 */
@Controller
public class PhotoController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    private final PhotoService photoService;

    // Get student name from environment variable or use default
    private final String studentName = System.getenv().getOrDefault("STUDENT_NAME", "Tabari Linus");
    private final String labName = "Photo Uploader Lab - ECS Fargate with S3 & RDS";

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Display the main gallery page
     */
    @GetMapping("/")
    public String index(Model model) {
        logger.info("Loading gallery homepage");

        try {
            List<PhotoDTO> photos = photoService.getAllPhotos();
            model.addAttribute("photos", photos);
            model.addAttribute("studentName", studentName);
            model.addAttribute("labName", labName);
            model.addAttribute("photoCount", photos.size());

            logger.info("Loaded {} photos for gallery display", photos.size());
            return "index";

        } catch (Exception e) {
            logger.error("Error loading gallery: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load gallery: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Handle photo upload
     */
    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              @RequestParam("description") String description,
                              RedirectAttributes redirectAttributes) {

        logger.info("Received photo upload request: {}", file.getOriginalFilename());

        try {
            // Validate inputs
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/";
            }

            if (description == null || description.trim().isEmpty()) {
                description = "No description provided";
            }

            // Upload photo
            PhotoDTO uploadedPhoto = photoService.uploadPhoto(file, description);

            redirectAttributes.addFlashAttribute("success",
                    "Successfully uploaded: " + uploadedPhoto.getFileName());
            logger.info("Successfully uploaded photo: {}", uploadedPhoto.getId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload attempt: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());

        } catch (IOException e) {
            logger.error("IO error during file upload: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Upload failed: File processing error");

        } catch (Exception e) {
            logger.error("Unexpected error during upload: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }

        return "redirect:/";
    }

    /**
     * Delete a photo
     */
    @PostMapping("/delete/{id}")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Received request to delete photo: {}", id);

        try {
            photoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Photo deleted successfully");
            logger.info("Successfully deleted photo: {}", id);

        } catch (Exception e) {
            logger.error("Error deleting photo {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete photo: " + e.getMessage());
        }

        return "redirect:/";
    }

    /**
     * Health check endpoint for ALB
     */
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }

    /**
     *
     * Actuator health endpoint (alternative)
     */
    @GetMapping("/actuator/health")
    @ResponseBody
    public String actuatorHealth() {
        return "{\"status\":\"UP\"}";
    }
}
