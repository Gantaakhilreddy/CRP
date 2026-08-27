package com.college.booking.repository;

import com.college.booking.entity.Booking;
import com.college.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByBookingDate(LocalDate date);

    long countByBookingDate(LocalDate date);

    long countByStatus(BookingStatus status);

    Optional<Booking> findByCheckInToken(String token);

    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN BookingResource br ON br.booking = b
            WHERE br.resource.id = :resourceId
              AND b.bookingDate = :date
              AND b.status IN :statuses
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    List<Booking> findConflicts(
            @Param("resourceId") Long resourceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN BookingResource br ON br.booking = b
            WHERE br.resource.id = :resourceId
              AND b.bookingDate = :date
              AND b.status IN :statuses
            ORDER BY b.startTime
            """)
    List<Booking> findByResourceAndDate(
            @Param("resourceId") Long resourceId,
            @Param("date") LocalDate date,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.id = :userId
              AND b.bookingDate = :date
              AND b.status IN :statuses
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    List<Booking> findUserDoubleBookings(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = :status
              AND b.bookingDate = :date
              AND b.startTime <= :cutoff
            """)
    List<Booking> findNoShowCandidates(
            @Param("status") BookingStatus status,
            @Param("date") LocalDate date,
            @Param("cutoff") LocalTime cutoff
    );

    @Query("""
            SELECT COUNT(b) FROM Booking b
            JOIN BookingResource br ON br.booking = b
            WHERE br.resource.building.id = :buildingId
              AND b.bookingDate = :date
              AND b.status IN :statuses
            """)
    long countBuildingBookingsOnDate(
            @Param("buildingId") Long buildingId,
            @Param("date") LocalDate date,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.bookingDate BETWEEN :from AND :to
            """)
    List<Booking> findBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
