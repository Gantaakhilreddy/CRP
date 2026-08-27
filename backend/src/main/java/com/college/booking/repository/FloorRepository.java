package com.college.booking.repository;

import com.college.booking.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByBuildingIdOrderByLevelAsc(Long buildingId);
    long countByBuildingId(Long buildingId);
}
