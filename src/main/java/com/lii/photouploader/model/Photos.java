package com.lii.photouploader.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "presigned_url", length = 2000)
    private String presignedUrl;

    @Column(name = "url_expiry_time")
    private LocalDateTime urlExpiryTime;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (urlExpiryTime == null) {
            // Set expiry to 2 days from now as per requirement
            urlExpiryTime = LocalDateTime.now().plusDays(2);
        }
    }

    public boolean isUrlExpired() {
        return LocalDateTime.now().isAfter(urlExpiryTime);
    }
}
