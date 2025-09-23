package com.lii.photouploadapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class S3Config {

    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        logger.info("Initializing S3 Client for region: {}", awsRegion);

        /**
         * Create S3 client bean
         * Uses ECS task role credentials when running in Fargate
         */
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Create S3 Presigner bean for generating presigned URLs
     */
    @Bean
    public S3Presigner s3Presigner() {
        logger.info("Initializing S3 Presigner for region: {}", awsRegion);

        return S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
