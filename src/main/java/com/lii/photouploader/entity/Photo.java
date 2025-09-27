package com.lii.photouploader.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String imageName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String s3ObjectKey;

    @Column(length = 2000)
    private String presignedUrl;

    private LocalDateTime presignedUrlExpiry;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Photo() {
        this.createdAt = LocalDateTime.now();
    }

    public Photo(String imageName, String description, String s3ObjectKey) {
        this();
        this.imageName = imageName;
        this.description = description;
        this.s3ObjectKey = s3ObjectKey;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getS3ObjectKey() { return s3ObjectKey; }
    public void setS3ObjectKey(String s3ObjectKey) { this.s3ObjectKey = s3ObjectKey; }

    public String getPresignedUrl() { return presignedUrl; }
    public void setPresignedUrl(String presignedUrl) { this.presignedUrl = presignedUrl; }

    public LocalDateTime getPresignedUrlExpiry() { return presignedUrlExpiry; }
    public void setPresignedUrlExpiry(LocalDateTime presignedUrlExpiry) { this.presignedUrlExpiry = presignedUrlExpiry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}