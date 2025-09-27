package com.lii.photouploader.service;

import com.lii.photouploader.entity.Photo;
import com.lii.photouploader.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    public Photo uploadPhoto(MultipartFile file, String description) throws IOException {
        // Validate file
        validateImageFile(file);

        // Generate unique key for S3
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String s3ObjectKey = UUID.randomUUID().toString() + fileExtension;

        // Upload to S3
        s3Service.uploadFile(s3ObjectKey, file.getBytes(), file.getContentType());

        // Generate presigned URL
        String presignedUrl = s3Service.generatePresignedUrl(s3ObjectKey);

        // Create and save photo entity
        Photo photo = new Photo(file.getOriginalFilename(), description, s3ObjectKey);
        photo.setPresignedUrl(presignedUrl);
        photo.setPresignedUrlExpiry(s3Service.calculateExpiryTime());

        return photoRepository.save(photo);
    }

    public List<Photo> getAllPhotos() {
        List<Photo> photos = photoRepository.findAll();

        // Check and refresh expired presigned URLs
        for (Photo photo : photos) {
            if (s3Service.isUrlExpired(photo.getPresignedUrlExpiry())) {
                String newPresignedUrl = s3Service.generatePresignedUrl(photo.getS3ObjectKey());
                photo.setPresignedUrl(newPresignedUrl);
                photo.setPresignedUrlExpiry(s3Service.calculateExpiryTime());
                photoRepository.save(photo);
            }
        }

        return photoRepository.findAll();
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }

        // Additional validation: check file signature
        if (!isValidImageSignature(file.getBytes())) {
            throw new IllegalArgumentException("File content does not match image type");
        }
    }

    private boolean isValidImageSignature(byte[] fileBytes) {
        if (fileBytes.length < 4) return false;

        // Check for common image file signatures
        return (fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8) || // JPEG
                (fileBytes[0] == (byte) 0x89 && fileBytes[1] == (byte) 0x50 &&
                        fileBytes[2] == (byte) 0x4E && fileBytes[3] == (byte) 0x47) || // PNG
                (fileBytes[0] == (byte) 'G' && fileBytes[1] == (byte) 'I' &&
                        fileBytes[2] == (byte) 'F') || // GIF
                (fileBytes[0] == (byte) 'R' && fileBytes[1] == (byte) 'I' &&
                        fileBytes[2] == (byte) 'F' && fileBytes[3] == (byte) 'F'); // WebP
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}