package com.lii.photouploadapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PhotoUploadAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(PhotoUploadAppApplication.class);
    public static void main(String[] args) {

        SpringApplication.run(PhotoUploadAppApplication.class, args);
        logger.info("Photo Upload Application Started Successfully!");
        logger.info("Application is running in ECS Fargate environment");
    }

}
