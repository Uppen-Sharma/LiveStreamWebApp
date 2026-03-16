package com.example.stream.repository;

import com.example.stream.model.StreamQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamQualityRepository extends JpaRepository<StreamQuality, Long> {
    
    // Find all active qualities ordered by sort order
    List<StreamQuality> findByIsActiveTrueOrderBySortOrderAsc();
    
    // Find quality by name
    Optional<StreamQuality> findByQualityName(String qualityName);
    
    // Find quality by name and active status
    Optional<StreamQuality> findByQualityNameAndIsActiveTrue(String qualityName);
    
    // Get all quality names
    @Query("SELECT sq.qualityName FROM StreamQuality sq WHERE sq.isActive = true ORDER BY sq.sortOrder")
    List<String> findAllActiveQualityNames();
    
    // Get quality labels for dropdown
    @Query("SELECT sq.qualityName, sq.qualityLabel FROM StreamQuality sq WHERE sq.isActive = true ORDER BY sq.sortOrder")
    List<Object[]> getQualityLabels();
} 