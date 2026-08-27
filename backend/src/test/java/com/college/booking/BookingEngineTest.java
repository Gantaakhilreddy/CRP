package com.college.booking;

import com.college.booking.dto.AuthDtos.LoginRequest;
import com.college.booking.dto.AuthDtos.RegisterRequest;
import com.college.booking.dto.BookingDtos.CreateBookingRequest;
import com.college.booking.entity.Building;
import com.college.booking.entity.Floor;
import com.college.booking.entity.Resource;
import com.college.booking.entity.ResourceType;
import com.college.booking.entity.User;
import com.college.booking.enums.BuildingKind;
import com.college.booking.enums.ResourceKind;
import com.college.booking.enums.ResourceStatus;
import com.college.booking.enums.Role;
import com.college.booking.repository.BuildingRepository;
import com.college.booking.repository.FloorRepository;
import com.college.booking.repository.ResourceRepository;
import com.college.booking.repository.ResourceTypeRepository;
import com.college.booking.repository.UserRepository;
import com.college.booking.service.AvailabilityService;
import com.college.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingEngineTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookingService bookingService;
    @Autowired AvailabilityService availabilityService;
    @Autowired UserRepository userRepository;
    @Autowired ResourceRepository resourceRepository;
    @Autowired ResourceTypeRepository resourceTypeRepository;
    @Autowired BuildingRepository buildingRepository;
    @Autowired FloorRepository floorRepository;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;

    private User student;
    private User professor;
    private User admin;
    private Resource room;

    @BeforeEach
    void setup() {
        ResourceType type = new ResourceType();
        type.setCode("CLASSROOM");
        type.setName("Classroom");
        type.setKind(ResourceKind.CLASSROOM);
        type = resourceTypeRepository.save(type);

        Building building = new Building();
        building.setName("Block A");
        building.setCode("LOYALTY1");
        building.setKind(BuildingKind.ACADEMIC);
        building.setBookable(true);
        building = buildingRepository.save(building);

        Floor floor = new Floor();
        floor.setBuilding(building);
        floor.setName("First Floor");
        floor.setLevel(1);
        floor = floorRepository.save(floor);

        room = new Resource();
        room.setName("Room 204");
        room.setCode("A-204");
        room.setResourceType(type);
        room.setFloor(floor);
        room.setBuilding(building);
        room.setCapacity(60);
        room.setOperationalStatus(ResourceStatus.AVAILABLE);
        room.setEnabled(true);
        room.setWorkingHoursStart(LocalTime.of(8, 0));
        room.setWorkingHoursEnd(LocalTime.of(18, 0));
        room.setQrToken(UUID.randomUUID().toString());
        room = resourceRepository.save(room);

        professor = saveUser("Dr Test", "professor@test.com", Role.PROFESSOR);
        admin = saveUser("Admin Test", "admin@test.com", Role.ADMIN);
        student = saveUser("Student Test", "student@test.com", Role.STUDENT);
        student.setAssignedProfessor(professor);
        userRepository.save(student);
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        RegisterRequest register = new RegisterRequest("Ada", "ada@test.com", "Password@123", Role.STUDENT, "CSE", null);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest bad = new LoginRequest("ada@test.com", "wrong");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentBookingStartsAsProfessorPending() {
        var view = bookingService.create(student, request(room.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0)));
        assertThat(view.status().name()).isEqualTo("PENDING_PROFESSOR");
        assertThat(availabilityService.check(room.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0)).available()).isFalse();
    }

    @Test
    void overlappingBookingIsRejected() {
        bookingService.create(student, request(room.getId(), LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(16, 0)));
        assertThatThrownBy(() -> bookingService.create(professor, request(room.getId(), LocalDate.now().plusDays(2),
                LocalTime.of(15, 0), LocalTime.of(17, 0))))
                .hasMessageContaining("already booked");
    }

    @Test
    void outsideHoursIsUnavailable() {
        var result = availabilityService.check(room.getId(), LocalDate.now().plusDays(3),
                LocalTime.of(19, 0), LocalTime.of(20, 0));
        assertThat(result.available()).isFalse();
        assertThat(result.reason()).contains("working hours");
    }

    @Test
    void professorThenAdminConfirms() {
        var created = bookingService.create(student, request(room.getId(), LocalDate.now().plusDays(4),
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        var afterProf = bookingService.approve(created.id(), professor, null);
        assertThat(afterProf.status().name()).isEqualTo("PENDING_ADMIN");
        var afterAdmin = bookingService.approve(created.id(), admin, null);
        assertThat(afterAdmin.status().name()).isEqualTo("CONFIRMED");
    }

    @Test
    void calendarEventsUseIsoLocalDateTime() {
        bookingService.create(admin, request(room.getId(), LocalDate.now().plusDays(6),
                LocalTime.of(10, 0), LocalTime.of(12, 0)));
        var events = bookingService.calendarEvents(admin, LocalDate.now(), LocalDate.now().plusDays(10));
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).start()).contains("T10:00:00");
        assertThat(events.get(0).end()).contains("T12:00:00");
        assertThat(events.get(0).url()).startsWith("/bookings/");
    }

    @Test
    void cancelReleasesSlot() {
        var created = bookingService.create(admin, request(room.getId(), LocalDate.now().plusDays(5),
                LocalTime.of(11, 0), LocalTime.of(13, 0)));
        bookingService.cancel(created.id(), admin);
        assertThat(availabilityService.check(room.getId(), LocalDate.now().plusDays(5),
                LocalTime.of(11, 0), LocalTime.of(13, 0)).available()).isTrue();
    }

    private CreateBookingRequest request(Long resourceId, LocalDate date, LocalTime start, LocalTime end) {
        return new CreateBookingRequest(List.of(resourceId), null, date, start, end,
                "Test", "Unit test", 10, null, null, null, "RESOURCE");
    }

    private User saveUser(String name, String email, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode("Password@123"));
        u.setRole(role);
        u.setEnabled(true);
        u.setNoShowCount(0);
        return userRepository.save(u);
    }
}
