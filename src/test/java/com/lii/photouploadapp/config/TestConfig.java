package com.lii.photouploadapp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.mockito.Mockito;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public S3Client mockS3Client() {
        return Mockito.mock(S3Client.class);
    }

    @Bean
    @Primary
    public S3Presigner mockS3Presigner() {
        return Mockito.mock(S3Presigner.class);
    }
}
