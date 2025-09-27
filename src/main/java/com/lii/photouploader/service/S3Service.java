package com.lii.photouploader.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucketName;

    public S3Service() {
        // Bucket name will be injected via environment variables
        this.bucketName = System.getenv("S3_BUCKET_NAME");
        Region region = Region.EU_CENTRAL_1;

        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        this.presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void uploadFile(String key, byte[] fileContent, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
    }

    public String generatePresignedUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofDays(2)) // 2 days expiry
                .build();

        String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
        presigner.close();

        return presignedUrl;
    }

    public LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plusDays(2); // 2 days from now
    }

    public boolean isUrlExpired(LocalDateTime expiryTime) {
        return expiryTime == null || LocalDateTime.now().isAfter(expiryTime);
    }
}