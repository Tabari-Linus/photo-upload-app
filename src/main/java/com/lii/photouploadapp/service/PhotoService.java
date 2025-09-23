package com.lii.photouploadapp.service;

import com.lii.photouploadapp.dto.PhotoDTO;
import com.lii.photouploadapp.model.Photo;
import com.lii.photouploadapp.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing photo operations
 * Coordinates between S3 storage and database operations
 */
@Service
@Transactional
public class PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

    private final PhotoRepository photoRepository;
    private final S3Service s3Service;

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed image types
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public PhotoService(PhotoRepository photoRepository, S3Service s3Service) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
    }


    /**
     * Upload a photo with description
     * @param file The image file to upload
     * @param description Description of the photo
     * @return The created PhotoDTO
     */
    public PhotoDTO uploadPhoto(MultipartFile file, String description) throws IOException {
        logger.info("Processing photo upload: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        try {
            // Upload to S3
            String objectKey = s3Service.uploadFile(file);

            // Generate presigned URL
            String presignedUrl = s3Service.generatePresignedUrl(objectKey);

            // Create database entry
            Photo photo = new Photo();
            photo.setObjectKey(objectKey);
            photo.setDescription(description);
            photo.setPresignedUrl(presignedUrl);
            photo.setFileName(file.getOriginalFilename());
            photo.setFileSize(file.getSize());
            photo.setContentType(file.getContentType());
            photo.setUrlExpiryTime(LocalDateTime.now().plusDays(2)); // 2 days as per requirement

            Photo savedPhoto = photoRepository.save(photo);
            logger.info("Successfully saved photo metadata with ID: {}", savedPhoto.getId());

            return convertToDTO(savedPhoto);

        } catch (Exception e) {
            logger.error("Failed to upload photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
        }
    }

    /**
     * Get all photos from the gallery
     * @return List of PhotoDTOs
     */
    public List<PhotoDTO> getAllPhotos() {
        logger.info("Fetching all photos from gallery");

        List<Photo> photos = photoRepository.findAllByOrderByUploadedAtDesc();

        // Check and refresh expired URLs
        photos = photos.stream()
                .map(this::refreshUrlIfExpired)
                .collect(Collectors.toList());

        logger.info("Retrieved {} photos from gallery", photos.size());
        return photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a single photo by ID
     * @param id Photo ID
     * @return PhotoDTO or null if not found
     */
    public PhotoDTO getPhotoById(Long id) {
        logger.info("Fetching photo with ID: {}", id);

        return photoRepository.findById(id)
                .map(this::refreshUrlIfExpired)
                .map(this::convertToDTO)
                .orElse(null);
    }


    /**
     * Delete a photo by ID
     * @param id Photo ID to delete
     */
    @Transactional
    public void deletePhoto(Long id) {
        logger.info("Deleting photo with ID: {}", id);

        photoRepository.findById(id).ifPresent(photo -> {
            // Delete from S3
            s3Service.deleteFile(photo.getObjectKey());

            // Delete from database
            photoRepository.delete(photo);
            logger.info("Successfully deleted photo with ID: {}", id);
        });
    }

    /**
     * Refresh presigned URL if expired
     * @param photo The photo entity
     * @return Updated photo entity
     */
    private Photo refreshUrlIfExpired(Photo photo) {
        if (photo.isUrlExpired()) {
            logger.info("Refreshing expired URL for photo: {}", photo.getId());

            String newPresignedUrl = s3Service.generatePresignedUrl(photo.getObjectKey());
            photo.setPresignedUrl(newPresignedUrl);
            photo.setUrlExpiryTime(LocalDateTime.now().plusDays(2));

            return photoRepository.save(photo);
        }
        return photo;
    }

    /**
     * Validate uploaded file
     * @param file The file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WEBP images are allowed");
        }
    }

    /**
     * Convert Photo entity to DTO
     * @param photo The photo entity
     * @return PhotoDTO
     */
    private PhotoDTO convertToDTO(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .description(photo.getDescription())
                .presignedUrl(photo.getPresignedUrl())
                .contentType(photo.getContentType())
                .fileSize(photo.getFileSize())
                .uploadedAt(photo.getUploadedAt())
                .urlExpired(photo.isUrlExpired())
                .build();
    }

    /**
     * Refresh all expired URLs (can be called by a scheduled task)
     */
    @Transactional
    public void refreshAllExpiredUrls() {
        logger.info("Starting batch refresh of expired URLs");

        List<Photo> expiredPhotos = photoRepository.findPhotosWithExpiredUrls(LocalDateTime.now());

        for (Photo photo : expiredPhotos) {
            try {
                String newPresignedUrl = s3Service.generatePresignedUrl(photo.getObjectKey());
                photo.setPresignedUrl(newPresignedUrl);
                photo.setUrlExpiryTime(LocalDateTime.now().plusDays(2));
                photoRepository.save(photo);
                logger.info("Refreshed URL for photo: {}", photo.getId());
            } catch (Exception e) {
                logger.error("Failed to refresh URL for photo {}: {}", photo.getId(), e.getMessage());
            }
        }

        logger.info("Completed batch refresh. Refreshed {} URLs", expiredPhotos.size());
    }
}