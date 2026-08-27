package com.college.booking.service;

import com.college.booking.dto.BookingDtos.BookingView;
import com.college.booking.entity.Booking;
import com.college.booking.entity.Building;
import com.college.booking.entity.Resource;
import com.college.booking.entity.User;
import com.college.booking.enums.BookingStatus;
import com.college.booking.enums.ResourceStatus;
import com.college.booking.repository.BookingApprovalRepository;
import com.college.booking.repository.BookingRepository;
import com.college.booking.repository.BuildingRepository;
import com.college.booking.repository.FloorRepository;
import com.college.booking.repository.IssueRepository;
import com.college.booking.repository.ResourceRepository;
import com.college.booking.enums.ApprovalStatus;
import com.college.booking.enums.IssueStatus;
import com.college.booking.enums.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final BookingApprovalRepository approvalRepository;
    private final IssueRepository issueRepository;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    public DashboardService(BuildingRepository buildingRepository, FloorRepository floorRepository,
                            ResourceRepository resourceRepository, BookingRepository bookingRepository,
                            BookingApprovalRepository approvalRepository, IssueRepository issueRepository,
                            AvailabilityService availabilityService, BookingService bookingService) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.approvalRepository = approvalRepository;
        this.issueRepository = issueRepository;
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
    }

    public Map<String, Object> forUser(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", Map.of("id", user.getId(), "name", user.getFullName(), "role", user.getRole().name()));
        List<Booking> mine = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(user.getId());
        data.put("pending", mine.stream().filter(b ->
                b.getStatus() == BookingStatus.PENDING_PROFESSOR || b.getStatus() == BookingStatus.PENDING_ADMIN).count());
        data.put("confirmed", mine.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
        data.put("completed", mine.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count());
        data.put("noShows", user.getNoShowCount());
        Booking upcoming = mine.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN
                        || b.getStatus() == BookingStatus.PENDING_ADMIN || b.getStatus() == BookingStatus.PENDING_PROFESSOR)
                .filter(b -> !b.getBookingDate().isBefore(LocalDate.now()))
                .findFirst().orElse(null);
        data.put("upcoming", upcoming == null ? null : bookingService.toView(upcoming));
        if (user.getRole() == Role.PROFESSOR || user.getRole() == Role.ADMIN) {
            data.put("pendingApprovals", bookingService.pendingFor(user).size());
        }
        if (user.getRole() == Role.ADMIN) {
            data.putAll(adminStats());
        }
        data.put("live", liveCampus());
        return data;
    }

    public Map<String, Object> adminStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        long resources = resourceRepository.countByEnabledTrue();
        List<Resource> all = resourceRepository.findAll().stream().filter(Resource::isEnabled).toList();
        int available = availabilityService.countAvailableNow(all);
        data.put("totalBuildings", buildingRepository.count());
        data.put("totalFloors", floorRepository.count());
        data.put("totalResources", resources);
        data.put("availableNow", available);
        data.put("bookingsToday", bookingRepository.countByBookingDate(LocalDate.now()));
        data.put("pendingApprovals",
                approvalRepository.countByRequiredRoleAndStatus(Role.ADMIN, ApprovalStatus.PENDING)
                        + approvalRepository.countByRequiredRoleAndStatus(Role.PROFESSOR, ApprovalStatus.PENDING));
        data.put("occupancyPercent", resources == 0 ? 0 : Math.round((1 - available / (double) resources) * 100));
        data.put("openIssues", issueRepository.findByStatus(IssueStatus.REPORTED).size()
                + issueRepository.findByStatus(IssueStatus.IN_PROGRESS).size());
        data.put("bookingTrends", bookingTrends());
        data.put("buildingUsage", buildingUsage());
        data.put("peakHours", peakHours());
        data.put("statusMix", statusMix());
        data.put("heatmap", heatmap());
        data.put("underutilized", underutilized());
        return data;
    }

    public Map<String, Object> liveCampus() {
        List<Resource> all = resourceRepository.findAll().stream().filter(Resource::isEnabled).toList();
        int available = 0, booked = 0, maint = 0, blocked = 0;
        for (Resource r : all) {
            ResourceStatus s = availabilityService.liveStatus(r);
            switch (s) {
                case AVAILABLE -> available++;
                case BOOKED, PENDING -> booked++;
                case MAINTENANCE -> maint++;
                case BLOCKED, OUT_OF_SERVICE -> blocked++;
                default -> available++;
            }
        }
        int total = Math.max(all.size(), 1);
        Map<String, Object> live = new LinkedHashMap<>();
        live.put("available", available);
        live.put("occupied", booked);
        live.put("maintenance", maint);
        live.put("blocked", blocked);
        live.put("occupancyPercent", Math.round((booked / (double) total) * 100));
        return live;
    }

    public List<Map<String, Object>> heatmap() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Building b : buildingRepository.findAll()) {
            List<Resource> resources = resourceRepository.findByBuildingIdAndEnabledTrue(b.getId());
            int available = availabilityService.countAvailableNow(resources);
            int total = Math.max(resources.size(), 1);
            int usedPct = (int) Math.round(((total - available) / (double) total) * 100);
            String color = usedPct >= 75 ? "HIGH" : usedPct >= 40 ? "MEDIUM" : "LOW";
            rows.add(Map.of(
                    "buildingId", b.getId(),
                    "name", b.getName(),
                    "code", b.getCode(),
                    "percent", usedPct,
                    "level", color,
                    "available", available,
                    "total", resources.size()
            ));
        }
        return rows;
    }

    private List<Map<String, Object>> bookingTrends() {
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            rows.add(Map.of("date", d.toString(), "count", bookingRepository.countByBookingDate(d)));
        }
        return rows;
    }

    private List<Map<String, Object>> buildingUsage() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Building b : buildingRepository.findAll()) {
            long count = bookingRepository.countBuildingBookingsOnDate(b.getId(), LocalDate.now(),
                    EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.PENDING_ADMIN,
                            BookingStatus.PENDING_PROFESSOR, BookingStatus.COMPLETED));
            rows.add(Map.of("building", b.getName(), "bookings", count));
        }
        return rows;
    }

    private List<Map<String, Object>> peakHours() {
        Map<Integer, Long> hours = new HashMap<>();
        bookingRepository.findBetween(LocalDate.now().minusDays(14), LocalDate.now()).forEach(b -> {
            int h = b.getStartTime().getHour();
            hours.put(h, hours.getOrDefault(h, 0L) + 1);
        });
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            rows.add(Map.of("hour", String.format("%02d:00", h), "count", hours.getOrDefault(h, 0L)));
        }
        return rows;
    }

    private Map<String, Long> statusMix() {
        Map<String, Long> mix = new LinkedHashMap<>();
        for (BookingStatus s : BookingStatus.values()) {
            mix.put(s.name(), bookingRepository.countByStatus(s));
        }
        return mix;
    }

    private List<Map<String, Object>> underutilized() {
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate from = LocalDate.now().minusDays(14);
        LocalDate to = LocalDate.now();
        List<Booking> window = bookingRepository.findBetween(from, to);
        for (Resource r : resourceRepository.findAll()) {
            if (!r.isEnabled() || r.getCapacity() == null || r.getCapacity() < 40) {
                continue;
            }
            long hours = window.stream()
                    .filter(b -> bookingService.toView(b).resources().stream().anyMatch(br -> br.id().equals(r.getId())))
                    .mapToLong(b -> Math.max(1, java.time.Duration.between(b.getStartTime(), b.getEndTime()).toHours()))
                    .sum();
            double availableHours = 14 * 10.0;
            int util = (int) Math.round((hours / availableHours) * 100);
            if (util < 35) {
                rows.add(Map.of(
                        "resourceId", r.getId(),
                        "name", r.getName(),
                        "capacity", r.getCapacity(),
                        "utilization", util,
                        "recommendation", "Consider assigning smaller rooms for similar events and reserving "
                                + r.getName() + " for large groups."
                ));
            }
        }
        return rows.stream().limit(8).toList();
    }

    public List<Map<String, Object>> utilization(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(7);
        if (to == null) to = LocalDate.now();
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1);
        double availableHours = days * 10.0;
        List<Booking> window = bookingRepository.findBetween(from, to);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Resource r : resourceRepository.findAll()) {
            if (!r.isEnabled()) continue;
            long bookedHours = window.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getStatus() != BookingStatus.REJECTED)
                    .filter(b -> bookingResourceMatch(b, r.getId()))
                    .mapToLong(b -> Math.max(1, java.time.Duration.between(b.getStartTime(), b.getEndTime()).toHours()))
                    .sum();
            int pct = (int) Math.round((bookedHours / availableHours) * 100);
            rows.add(Map.of(
                    "resourceId", r.getId(),
                    "name", r.getName(),
                    "building", r.getBuilding().getName(),
                    "type", r.getResourceType().getName(),
                    "bookedHours", bookedHours,
                    "availableHours", availableHours,
                    "utilization", Math.min(100, pct)
            ));
        }
        return rows;
    }

    private boolean bookingResourceMatch(Booking b, Long resourceId) {
        return bookingService.toView(b).resources().stream().anyMatch(r -> r.id().equals(resourceId));
    }

    public BookingView upcoming(User user) {
        return (BookingView) forUser(user).get("upcoming");
    }

    @SuppressWarnings("unused")
    private LocalTime now() {
        return LocalTime.now();
    }
}
