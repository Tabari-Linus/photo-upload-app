package com.lii.photouploadapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {

    private Long id;
    private String fileName;
    private String description;
    private String presignedUrl;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private boolean urlExpired;

    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.2f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
    }

    public String getFormattedUploadDate() {
        if (uploadedAt == null) return "Unknown";
        return uploadedAt.toString();
    }
}
