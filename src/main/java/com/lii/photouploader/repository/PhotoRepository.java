package com.lii.photouploader.repository;


import com.lii.photouploader.model.Photos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photos, Long> {

        Optional<Photos> findByObjectKey(String objectKey);

        List<Photos> findAllByOrderByUploadedAtDesc();

        @Query("SELECT p FROM Photos p WHERE p.urlExpiryTime <= ?1")
        List<Photos> findPhotosWithExpiredUrls(LocalDateTime currentTime);

        boolean existsByObjectKey(String objectKey);

        void deleteByObjectKey(String objectKey);
    }
