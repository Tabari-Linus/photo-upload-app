package com.lii.photouploader.repository;

import com.lii.photouploadapp.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

        Optional<Photo> findByObjectKey(String objectKey);

        List<Photo> findAllByOrderByUploadedAtDesc();

        @Query("SELECT p FROM Photo p WHERE p.urlExpiryTime < :currentTime")
        List<Photo> findPhotosWithExpiredUrls(LocalDateTime currentTime);

        boolean existsByObjectKey(String objectKey);

        void deleteByObjectKey(String objectKey);
    }
