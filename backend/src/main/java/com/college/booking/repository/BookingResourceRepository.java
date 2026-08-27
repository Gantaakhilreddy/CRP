package com.college.booking.repository;

import com.college.booking.entity.BookingResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingResourceRepository extends JpaRepository<BookingResource, Long> {
    List<BookingResource> findByBookingId(Long bookingId);
    List<BookingResource> findByResourceId(Long resourceId);
}
