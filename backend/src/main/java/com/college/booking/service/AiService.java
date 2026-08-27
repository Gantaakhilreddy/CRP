package com.college.booking.service;

import com.college.booking.dto.AiDtos.ChatResponse;
import com.college.booking.dto.AiDtos.ChatTurn;
import com.college.booking.dto.AiDtos.Intent;
import com.college.booking.dto.AiDtos.InterpretResponse;
import com.college.booking.dto.AiDtos.Recommendation;
import com.college.booking.dto.CampusDtos.ResourceCard;
import com.college.booking.entity.Building;
import com.college.booking.entity.Resource;
import com.college.booking.entity.User;
import com.college.booking.repository.BookingRepository;
import com.college.booking.repository.BuildingRepository;
import com.college.booking.repository.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {

    private static final Set<String> TYPES = Set.of(
            "CLASSROOM", "LABORATORY", "SEMINAR_HALL", "LIBRARY",
            "AUDITORIUM", "EXAMINATION_HALL", "SPORTS_FACILITY", "EQUIPMENT"
    );

    private final GroqClient groqClient;
    private final CampusService campusService;
    private final AvailabilityService availabilityService;
    private final ResourceRepository resourceRepository;
    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public AiService(GroqClient groqClient, CampusService campusService, AvailabilityService availabilityService,
                     ResourceRepository resourceRepository, BuildingRepository buildingRepository,
                     BookingRepository bookingRepository, DashboardService dashboardService, ObjectMapper objectMapper) {
        this.groqClient = groqClient;
        this.campusService = campusService;
        this.availabilityService = availabilityService;
        this.resourceRepository = resourceRepository;
        this.buildingRepository = buildingRepository;
        this.bookingRepository = bookingRepository;
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    public InterpretResponse interpret(String prompt, User user) {
        Intent intent = extractIntent(prompt);
        intent = sanitize(intent);
        List<Recommendation> recs = recommend(intent, user);
        String explanation = explain(intent, recs);
        return new InterpretResponse(intent, recs, explanation, groqClient.isConfigured());
    }

    public ChatResponse chat(String message, List<ChatTurn> history, User user) {
        Intent intent = extractIntent(message);
        intent = sanitize(intent);
        String lower = message.toLowerCase(Locale.ROOT);
        List<Map<String, Object>> data = new ArrayList<>();
        String facts;

        if (lower.contains("upcoming booking") || lower.contains("my booking")) {
            var mine = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(user.getId());
            facts = mine.isEmpty() ? "The user has no bookings." : mine.stream().limit(5)
                    .map(b -> b.getTitle() + " on " + b.getBookingDate() + " " + b.getStartTime() + "–" + b.getEndTime()
                            + " (" + b.getStatus() + ")")
                    .reduce((a, b) -> a + "; " + b).orElse("");
            mine.stream().limit(5).forEach(b -> data.add(Map.of("bookingId", b.getId(), "title", b.getTitle(), "status", b.getStatus())));
        } else if (lower.contains("available now") || lower.contains("available right now")) {
            List<ResourceCard> now = campusService.availableNow(null, null, null, user);
            facts = now.isEmpty() ? "No resources are available right now." :
                    now.stream().limit(12).map(r -> r.name() + " (" + r.buildingName() + ")").reduce((a, b) -> a + ", " + b).orElse("");
            now.stream().limit(12).forEach(r -> data.add(Map.of("id", r.id(), "name", r.name(), "building", r.buildingName())));
        } else if (lower.contains("where is") || lower.contains("which block") || lower.contains("largest")) {
            String q = searchableQuery(intent.query());
            if (q == null) {
                q = searchableQuery(message.replace("where is", "").replace("which block", "").trim());
            }
            List<Resource> found = resourceRepository.search(q,
                    null, null, intent.resourceType(), null, intent.capacity());
            if (found.isEmpty()) {
                facts = "No matching resource exists in the database.";
            } else {
                facts = found.stream().limit(8)
                        .map(r -> r.getName() + " is in " + r.getBuilding().getName() + ", " + r.getFloor().getName()
                                + " (capacity " + r.getCapacity() + ")")
                        .reduce((a, b) -> a + "; " + b).orElse("");
                found.stream().limit(8).forEach(r -> data.add(Map.of(
                        "id", r.getId(), "name", r.getName(), "building", r.getBuilding().getName(), "floor", r.getFloor().getName())));
            }
        } else {
            List<Recommendation> recs = recommend(intent, user);
            facts = recs.isEmpty()
                    ? "No matching resources exist in SQL for this request."
                    : recs.stream().map(r -> r.resourceName() + " score " + r.score() + " — " + r.reason())
                    .reduce((a, b) -> a + "; " + b).orElse("");
            recs.forEach(r -> data.add(Map.of("id", r.resourceId(), "name", r.resourceName(), "score", r.score(), "reason", r.reason())));
        }

        String reply;
        try {
            if (!groqClient.isConfigured()) {
                throw new IllegalStateException("GROQ_UNAVAILABLE");
            }
            reply = groqClient.chat(
                    "You are the VVIT campus assistant. Answer ONLY using the provided facts. Never invent rooms, buildings, or statistics. If facts are empty, say you could not find a match and suggest using the campus map or manual booking. Keep the tone helpful and concise.",
                    "User question: " + message + "\nFacts from the database:\n" + facts
            );
        } catch (Exception ex) {
            reply = composeFallbackReply(message, facts, recsFromData(data));
            return new ChatResponse(reply, data, false);
        }
        return new ChatResponse(reply, data, true);
    }

    private String composeFallbackReply(String message, String facts, boolean hasRecs) {
        if (facts == null || facts.isBlank()) {
            return "I could not find a match in the campus database. Try the campus map or the booking wizard.";
        }
        return facts;
    }

    private boolean recsFromData(List<Map<String, Object>> data) {
        return data != null && !data.isEmpty();
    }

    public String insights(String question) {
        Map<String, Object> stats = dashboardService.adminStats();
        String facts = stats.toString();
        try {
            return groqClient.chat(
                    "You are a campus operations analyst. Explain the provided statistics. Never invent numbers. If the question cannot be answered from the facts, say so.",
                    "Question: " + question + "\nStatistics: " + facts
            );
        } catch (Exception ex) {
            return "Here are the live campus statistics from SQL: " + facts;
        }
    }

    private Intent extractIntent(String prompt) {
        if (groqClient.isConfigured()) {
            try {
                String json = groqClient.chatJson(
                        "Extract a booking intent as JSON with keys: resourceType (CLASSROOM|LABORATORY|SEMINAR_HALL|LIBRARY|AUDITORIUM|EXAMINATION_HALL|null), capacity (int or null), date (YYYY-MM-DD or null, today is "
                                + LocalDate.now() + ", tomorrow is " + LocalDate.now().plusDays(1)
                                + "), startTime (HH:mm or null), endTime (HH:mm or null), requiredFacilities (array of codes like PROJECTOR, SMART_BOARD, AC, WIFI, AUDIO, MIC, COMPUTERS), building (string or null), query (short search phrase), intent (FIND|LOCATE|AVAILABILITY|BOOKINGS|ANALYTICS).",
                        prompt
                );
                return objectMapper.readValue(json, Intent.class);
            } catch (Exception ignored) {
                // fall through to heuristic parser
            }
        }
        return heuristic(prompt);
    }

    private Intent sanitize(Intent intent) {
        String type = intent.resourceType();
        if (type != null) {
            type = type.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
            if (type.contains("LAB")) type = "LABORATORY";
            if (type.contains("SEMINAR") || type.contains("HALL") && type.contains("SEM")) type = "SEMINAR_HALL";
            if (type.contains("CLASS")) type = "CLASSROOM";
            if (!TYPES.contains(type)) type = null;
        }
        LocalDate date = parseDate(intent.date());
        LocalTime start = parseTime(intent.startTime());
        LocalTime end = parseTime(intent.endTime());
        if (intent.query() != null) {
            Intent fromPrompt = heuristic(intent.query());
            LocalTime hs = parseTime(fromPrompt.startTime());
            LocalTime he = parseTime(fromPrompt.endTime());
            if (hs != null && he != null && he.isAfter(hs)) {
                start = hs;
                end = he;
            }
        }
        if (end != null && (end.equals(LocalTime.MIDNIGHT) || (start != null && !end.isAfter(start)))) {
            if (start != null && end.getHour() > 0 && end.getHour() < 12) {
                end = end.plusHours(12);
            }
        }
        Integer cap = intent.capacity() != null && intent.capacity() > 0 ? intent.capacity() : null;
        List<String> facilities = intent.requiredFacilities() == null ? List.of() : intent.requiredFacilities().stream()
                .map(f -> f.trim().toUpperCase(Locale.ROOT).replace(' ', '_'))
                .toList();
        return new Intent(type, cap, date == null ? null : date.toString(),
                start == null ? null : start.toString(),
                end == null ? null : end.toString(),
                facilities, intent.building(), intent.query(), intent.intent());
    }

    public List<Recommendation> recommend(Intent intent, User user) {
        Long buildingId = null;
        if (intent.building() != null && !intent.building().isBlank()) {
            buildingId = buildingRepository.findAll().stream()
                    .filter(b -> b.getName().toLowerCase(Locale.ROOT).contains(intent.building().toLowerCase(Locale.ROOT))
                            || b.getCode().equalsIgnoreCase(intent.building())
                            || (b.getVirtueName() != null && b.getVirtueName().toLowerCase(Locale.ROOT).contains(intent.building().toLowerCase(Locale.ROOT))))
                    .map(Building::getId)
                    .findFirst()
                    .orElse(null);
        }
        LocalDate date = parseDate(intent.date());
        LocalTime start = parseTime(intent.startTime());
        LocalTime end = parseTime(intent.endTime());
        String query = searchableQuery(intent.query());
        List<ResourceCard> cards = campusService.search(
                query, buildingId, null, intent.resourceType(), null,
                intent.capacity(), date, start, end, intent.requiredFacilities(), user);

        // If time filters removed everything because they were too strict, still score existing SQL resources.
        if (cards.isEmpty()) {
            cards = campusService.search(query, buildingId, null, intent.resourceType(), null,
                    intent.capacity(), null, null, null, intent.requiredFacilities(), user);
        }

        List<Recommendation> recs = new ArrayList<>();
        for (ResourceCard card : cards) {
            Resource resource = resourceRepository.findById(card.id()).orElseThrow();
            int score = 0;
            StringBuilder why = new StringBuilder();
            boolean available = true;
            if (date != null && start != null && end != null) {
                String reason = availabilityService.unavailableReason(resource, date, start, end);
                if (reason == null) {
                    score += 40;
                    why.append("Available for the requested time. ");
                } else {
                    available = false;
                    why.append(reason).append(" ");
                }
            } else if (availabilityService.isAvailableNow(resource)) {
                score += 25;
                why.append("Currently free. ");
            }
            if (intent.capacity() != null) {
                int cap = resource.getCapacity() == null ? 0 : resource.getCapacity();
                if (cap >= intent.capacity()) {
                    int waste = cap - intent.capacity();
                    int fit = Math.max(0, 25 - waste / 4);
                    score += fit;
                    if (waste <= 10) {
                        why.append("Capacity ").append(cap).append(" closely fits ").append(intent.capacity()).append(". ");
                    } else {
                        why.append("Capacity ").append(cap).append(" can host ").append(intent.capacity()).append(". ");
                    }
                }
            } else {
                score += 5;
            }
            if (intent.resourceType() != null && intent.resourceType().equalsIgnoreCase(card.typeCode())) {
                score += 15;
                why.append("Matches requested type. ");
            }
            if (intent.requiredFacilities() != null) {
                long matched = intent.requiredFacilities().stream()
                        .filter(f -> card.facilities().stream().anyMatch(cf -> cf.equalsIgnoreCase(f)
                                || cf.replace(" ", "_").equalsIgnoreCase(f)))
                        .count();
                score += (int) (matched * 5);
                if (matched > 0) {
                    why.append("Has the requested facilities. ");
                }
            }
            recs.add(new Recommendation(card.id(), card.name(), card.code(), score,
                    why.toString().isBlank() ? "Matches campus inventory." : why.toString().trim(), available));
        }
        recs.sort(Comparator.comparingInt(Recommendation::score).reversed());
        return recs.stream().limit(8).toList();
    }

    private String explain(Intent intent, List<Recommendation> recs) {
        if (recs.isEmpty()) {
            return "No matching resource exists in the campus database. Try a different type, capacity, or time, or book manually from the campus map.";
        }
        Recommendation top = recs.get(0);
        try {
            if (groqClient.isConfigured()) {
                return groqClient.chat(
                        "Write one short recommendation sentence using only the given facts. Do not invent rooms.",
                        "Requested: " + intent + " Best: " + top.resourceName() + " (" + top.resourceCode() + ") — " + top.reason()
                );
            }
        } catch (IllegalStateException ignored) {
        }
        return top.resourceName() + " is recommended. " + top.reason();
    }

    private Intent heuristic(String prompt) {
        String p = prompt.toLowerCase(Locale.ROOT);
        String type = null;
        if (p.contains("seminar")) type = "SEMINAR_HALL";
        else if (p.contains("lab")) type = "LABORATORY";
        else if (p.contains("library")) type = "LIBRARY";
        else if (p.contains("auditorium")) type = "AUDITORIUM";
        else if (p.contains("exam")) type = "EXAMINATION_HALL";
        else if (p.contains("class") || p.contains("room")) type = "CLASSROOM";
        Integer cap = null;
        var m = java.util.regex.Pattern.compile("(\\d+)\\s*(students|people|seats|computers)?").matcher(p);
        if (m.find()) {
            cap = Integer.parseInt(m.group(1));
        }
        LocalDate date = p.contains("tomorrow") ? LocalDate.now().plusDays(1)
                : p.contains("today") ? LocalDate.now() : null;
        LocalTime start = null;
        LocalTime end = null;
        var tm = java.util.regex.Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)").matcher(p);
        List<LocalTime> times = new ArrayList<>();
        while (tm.find() && times.size() < 2) {
            int h = Integer.parseInt(tm.group(1));
            int min = tm.group(2) == null ? 0 : Integer.parseInt(tm.group(2));
            String ap = tm.group(3);
            if (ap != null && ap.equals("pm") && h < 12) h += 12;
            if (ap != null && ap.equals("am") && h == 12) h = 0;
            if (h <= 23 && min <= 59) times.add(LocalTime.of(h, min));
        }
        if (times.size() >= 1) start = times.get(0);
        if (times.size() >= 2) end = times.get(1);
        List<String> fac = new ArrayList<>();
        if (p.contains("projector")) fac.add("PROJECTOR");
        if (p.contains("smart")) fac.add("SMART_BOARD");
        if (p.contains("ac") || p.contains("air")) fac.add("AC");
        if (p.contains("wifi") || p.contains("wi-fi")) fac.add("WIFI");
        String building = null;
        if (p.contains("block a") || p.contains("loyalty 1")) building = "LOYALTY1";
        if (p.contains("block b") || p.contains("loyalty 2")) building = "LOYALTY2";
        if (p.contains("block c") || p.contains("loyalty 3")) building = "LOYALTY3";
        if (p.contains("block d") || p.contains("loyalty 4")) building = "LOYALTY4";
        if (p.contains("central") || p.contains("wisdom")) building = "WISDOM";
        if (p.contains("siemens") || p.contains("honesty")) building = "HONESTY";
        if (p.contains("truth") || p.contains("university")) building = "TRUTH";
        String intent = p.contains("where") ? "LOCATE" : "FIND";
        return new Intent(type, cap, date == null ? null : date.toString(),
                start == null ? null : start.toString(), end == null ? null : end.toString(),
                fac, building, prompt, intent);
    }

    private String searchableQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String trimmed = query.trim();
        if (trimmed.length() > 32 || trimmed.split("\\s+").length > 4) {
            return null;
        }
        return trimmed;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String t = raw.trim().toLowerCase(Locale.ROOT).replace('.', ':');
        boolean pm = t.contains("pm");
        boolean am = t.contains("am");
        t = t.replace("pm", "").replace("am", "").trim();
        try {
            if (t.matches("\\d{1,2}")) {
                t = t + ":00";
            }
            if (t.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                t = t.substring(0, t.lastIndexOf(':'));
            }
            String[] parts = t.split(":");
            int hour = Integer.parseInt(parts[0]);
            int min = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            if (pm && hour < 12) hour += 12;
            if (am && hour == 12) hour = 0;
            if (hour == 24) hour = 0;
            return LocalTime.of(hour, min);
        } catch (Exception ex) {
            return null;
        }
    }
}
