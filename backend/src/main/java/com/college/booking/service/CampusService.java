package com.college.booking.service;

import com.college.booking.dto.CampusDtos.AvailabilityResponse;
import com.college.booking.dto.CampusDtos.BookingHistorySummary;
import com.college.booking.dto.CampusDtos.BuildingDetail;
import com.college.booking.dto.CampusDtos.BuildingSummary;
import com.college.booking.dto.CampusDtos.FloorMapDto;
import com.college.booking.dto.CampusDtos.FloorSummary;
import com.college.booking.dto.CampusDtos.HourSlot;
import com.college.booking.dto.CampusDtos.LayoutUpdate;
import com.college.booking.dto.CampusDtos.ResourceCard;
import com.college.booking.dto.CampusDtos.ResourceDetail;
import com.college.booking.entity.Building;
import com.college.booking.entity.Favorite;
import com.college.booking.entity.Floor;
import com.college.booking.entity.RecentlyVisited;
import com.college.booking.entity.Resource;
import com.college.booking.entity.ResourceFacility;
import com.college.booking.entity.ResourceType;
import com.college.booking.entity.User;
import com.college.booking.enums.ResourceKind;
import com.college.booking.enums.ResourceStatus;
import com.college.booking.exception.ApiException;
import com.college.booking.mapper.DtoMapper;
import com.college.booking.repository.BookingResourceRepository;
import com.college.booking.repository.BuildingRepository;
import com.college.booking.repository.FavoriteRepository;
import com.college.booking.repository.FloorRepository;
import com.college.booking.repository.RecentlyVisitedRepository;
import com.college.booking.repository.ResourceFacilityRepository;
import com.college.booking.repository.ResourceRepository;
import com.college.booking.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CampusService {

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceFacilityRepository resourceFacilityRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final FavoriteRepository favoriteRepository;
    private final RecentlyVisitedRepository recentlyVisitedRepository;
    private final BookingResourceRepository bookingResourceRepository;
    private final AvailabilityService availabilityService;
    private final DtoMapper mapper;

    public CampusService(BuildingRepository buildingRepository, FloorRepository floorRepository,
                         ResourceRepository resourceRepository, ResourceFacilityRepository resourceFacilityRepository,
                         ResourceTypeRepository resourceTypeRepository, FavoriteRepository favoriteRepository,
                         RecentlyVisitedRepository recentlyVisitedRepository,
                         BookingResourceRepository bookingResourceRepository,
                         AvailabilityService availabilityService, DtoMapper mapper) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.resourceRepository = resourceRepository;
        this.resourceFacilityRepository = resourceFacilityRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.favoriteRepository = favoriteRepository;
        this.recentlyVisitedRepository = recentlyVisitedRepository;
        this.bookingResourceRepository = bookingResourceRepository;
        this.availabilityService = availabilityService;
        this.mapper = mapper;
    }

    public List<BuildingSummary> campus(User user) {
        return buildingRepository.findAll().stream()
                .sorted(Comparator.comparing(Building::getId))
                .map(this::toBuildingSummary)
                .toList();
    }

    public BuildingDetail building(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Building not found."));
        List<FloorSummary> floors = floorRepository.findByBuildingIdOrderByLevelAsc(id).stream()
                .map(this::toFloorSummary)
                .toList();
        return new BuildingDetail(toBuildingSummary(building), floors);
    }

    public FloorMapDto floor(Long id, User user) {
        Floor floor = floorRepository.findById(id).orElseThrow(() -> ApiException.notFound("Floor not found."));
        List<ResourceCard> cards = resourceRepository.findByFloorIdAndEnabledTrue(id).stream()
                .map(r -> toCard(r, user))
                .toList();
        return new FloorMapDto(toFloorSummary(floor), cards);
    }

    @Transactional
    public ResourceDetail resource(Long id, User user, LocalDate date) {
        Resource resource = resourceRepository.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("That resource does not exist."));
        if (user != null) {
            RecentlyVisited visit = recentlyVisitedRepository.findByUserIdAndResourceId(user.getId(), id)
                    .orElseGet(RecentlyVisited::new);
            visit.setUser(user);
            visit.setResource(resource);
            visit.setVisitedAt(Instant.now());
            recentlyVisitedRepository.save(visit);
        }
        LocalDate target = date == null ? LocalDate.now() : date;
        List<HourSlot> timeline = availabilityService.timeline(resource, target);
        var bookings = bookingResourceRepository.findByResourceId(id);
        long total = bookings.size();
        long completed = bookings.stream().filter(br -> br.getBooking().getStatus().name().equals("COMPLETED")).count();
        long cancelled = bookings.stream().filter(br -> br.getBooking().getStatus().name().equals("CANCELLED")).count();
        long noShows = bookings.stream().filter(br -> br.getBooking().getStatus().name().equals("NO_SHOW")).count();
        return new ResourceDetail(toCard(resource, user), timeline, new BookingHistorySummary(total, completed, cancelled, noShows));
    }

    public AvailabilityResponse availability(Long resourceId, LocalDate date, LocalTime start, LocalTime end) {
        return availabilityService.check(resourceId, date, start, end);
    }

    public List<ResourceCard> search(String q, Long buildingId, Long floorId, String typeCode,
                                     String department, Integer minCapacity, LocalDate date,
                                     LocalTime start, LocalTime end, List<String> facilities, User user) {
        List<Resource> found = resourceRepository.search(emptyToNull(q), buildingId, floorId, emptyToNull(typeCode),
                emptyToNull(department), minCapacity);
        List<ResourceCard> cards = new ArrayList<>();
        for (Resource r : found) {
            if (facilities != null && !facilities.isEmpty()) {
                List<String> have = resourceFacilityRepository.findByResourceId(r.getId()).stream()
                        .map(rf -> rf.getFacility().getCode())
                        .toList();
                boolean ok = facilities.stream().allMatch(f -> have.stream().anyMatch(h -> h.equalsIgnoreCase(f)));
                if (!ok) {
                    continue;
                }
            }
            if (date != null && start != null && end != null) {
                if (availabilityService.unavailableReason(r, date, start, end) != null) {
                    continue;
                }
            }
            cards.add(toCard(r, user));
        }
        return cards;
    }

    public List<ResourceCard> availableNow(Long buildingId, Long floorId, String typeCode, User user) {
        List<Resource> resources = resourceRepository.search(null, buildingId, floorId, emptyToNull(typeCode), null, null);
        return resources.stream()
                .filter(availabilityService::isAvailableNow)
                .map(r -> toCard(r, user))
                .toList();
    }

    public ResourceCard byQr(String token, User user) {
        Resource resource = resourceRepository.findByQrToken(token)
                .orElseThrow(() -> ApiException.notFound("Unknown QR code."));
        return toCard(resource, user);
    }

    @Transactional
    public void toggleFavorite(User user, Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> ApiException.notFound("Resource not found."));
        favoriteRepository.findByUserIdAndResourceId(user.getId(), resourceId).ifPresentOrElse(
                favoriteRepository::delete,
                () -> {
                    Favorite f = new Favorite();
                    f.setUser(user);
                    f.setResource(resource);
                    favoriteRepository.save(f);
                }
        );
    }

    public List<ResourceCard> favorites(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(f -> toCard(f.getResource(), user))
                .toList();
    }

    public List<ResourceCard> recent(User user) {
        return recentlyVisitedRepository.findTop8ByUserIdOrderByVisitedAtDesc(user.getId()).stream()
                .map(v -> toCard(v.getResource(), user))
                .toList();
    }

    @Transactional
    public void saveLayout(Long floorId, List<LayoutUpdate> updates) {
        Floor floor = floorRepository.findById(floorId).orElseThrow(() -> ApiException.notFound("Floor not found."));
        for (LayoutUpdate u : updates) {
            Resource resource;
            if (u.resourceId() == null) {
                resource = new Resource();
                resource.setFloor(floor);
                resource.setBuilding(floor.getBuilding());
                resource.setQrToken(UUID.randomUUID().toString().replace("-", ""));
                resource.setEnabled(true);
                resource.setOperationalStatus(ResourceStatus.AVAILABLE);
                resource.setCapacity(30);
                resource.setWorkingHoursStart(LocalTime.of(8, 0));
                resource.setWorkingHoursEnd(LocalTime.of(18, 0));
            } else {
                resource = resourceRepository.findById(u.resourceId())
                        .orElseThrow(() -> ApiException.notFound("Resource not found."));
            }
            if (u.name() != null) {
                resource.setName(u.name());
                if (resource.getCode() == null) {
                    resource.setCode(u.name().replace(" ", "-").toUpperCase() + "-" + System.currentTimeMillis() % 10000);
                }
            }
            if (u.typeCode() != null) {
                ResourceType type = resourceTypeRepository.findByCode(u.typeCode())
                        .orElseThrow(() -> ApiException.notFound("Unknown resource type."));
                resource.setResourceType(type);
            } else if (resource.getResourceType() == null) {
                resource.setResourceType(resourceTypeRepository.findByCode("CLASSROOM").orElseThrow());
            }
            if (u.positionX() != null) resource.setPositionX(u.positionX());
            if (u.positionY() != null) resource.setPositionY(u.positionY());
            if (u.width() != null) resource.setWidth(u.width());
            if (u.height() != null) resource.setHeight(u.height());
            if (u.rotation() != null) resource.setRotation(u.rotation());
            resourceRepository.save(resource);
        }
    }

    public List<ResourceType> types() {
        return resourceTypeRepository.findAll();
    }

    private BuildingSummary toBuildingSummary(Building b) {
        List<Resource> resources = resourceRepository.findByBuildingIdAndEnabledTrue(b.getId());
        int floors = (int) floorRepository.countByBuildingId(b.getId());
        int available = availabilityService.countAvailableNow(resources);
        return mapper.toBuilding(b, floors, resources.size(), available);
    }

    private FloorSummary toFloorSummary(Floor floor) {
        List<Resource> resources = resourceRepository.findByFloorIdAndEnabledTrue(floor.getId());
        int classrooms = (int) resources.stream().filter(r -> r.getResourceType().getKind() == ResourceKind.CLASSROOM).count();
        int labs = (int) resources.stream().filter(r -> r.getResourceType().getKind() == ResourceKind.LABORATORY).count();
        int halls = (int) resources.stream().filter(r ->
                r.getResourceType().getKind() == ResourceKind.SEMINAR_HALL
                        || r.getResourceType().getKind() == ResourceKind.AUDITORIUM
                        || r.getResourceType().getKind() == ResourceKind.EXAMINATION_HALL).count();
        int libraries = (int) resources.stream().filter(r -> r.getResourceType().getKind() == ResourceKind.LIBRARY).count();
        int available = availabilityService.countAvailableNow(resources);
        return mapper.toFloor(floor, classrooms, labs, halls, libraries, resources.size(), available);
    }

    private ResourceCard toCard(Resource r, User user) {
        List<ResourceFacility> facilities = resourceFacilityRepository.findByResourceId(r.getId());
        boolean fav = user != null && favoriteRepository.existsByUserIdAndResourceId(user.getId(), r.getId());
        return mapper.toResource(r, facilities, fav, availabilityService.liveStatus(r));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
