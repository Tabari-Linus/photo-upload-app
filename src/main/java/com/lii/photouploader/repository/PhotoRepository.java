package com.lii.photouploader.repository;

import com.lii.photouploader.entity.Photos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photos, UUID> {
}
