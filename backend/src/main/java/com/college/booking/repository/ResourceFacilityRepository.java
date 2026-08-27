package com.college.booking.repository;

import com.college.booking.entity.ResourceFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceFacilityRepository extends JpaRepository<ResourceFacility, Long> {
    List<ResourceFacility> findByResourceId(Long resourceId);
    void deleteByResourceId(Long resourceId);
}
