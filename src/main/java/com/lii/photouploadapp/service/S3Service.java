package com.lii.photouploadapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.presigned.url.duration.days:2}")
    private int presignedUrlDurationDays;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }


    /**
     * Upload a file to S3 bucket
     * @param file The multipart file to upload
     * @return The S3 object key of the uploaded file
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectKey = "photos/" + UUID.randomUUID().toString() + fileExtension;

        logger.info("Uploading file to S3: {} with key: {}", originalFilename, objectKey);

        try {
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Upload file to S3
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("Successfully uploaded file to S3: {}", objectKey);
            return objectKey;

        } catch (S3Exception e) {
            logger.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a presigned URL for an S3 object
     * @param objectKey The S3 object key
     * @return The presigned URL
     */
    public String generatePresignedUrl(String objectKey) {
        logger.info("Generating presigned URL for object: {}", objectKey);

        try {
            // Create GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Create presign request with duration
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(presignedUrlDurationDays))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate presigned URL
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            logger.info("Generated presigned URL for object: {}", objectKey);
            return url;

        } catch (S3Exception e) {
            logger.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an object from S3
     * @param objectKey The S3 object key to delete
     */
    public void deleteFile(String objectKey) {
        logger.info("Deleting file from S3: {}", objectKey);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Successfully deleted file from S3: {}", objectKey);

        } catch (S3Exception e) {
            logger.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Check if an object exists in S3
     * @param objectKey The S3 object key
     * @return true if object exists, false otherwise
     */
    public boolean doesObjectExist(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking if object exists: {}", e.getMessage(), e);
            return false;
        }
    }
}
