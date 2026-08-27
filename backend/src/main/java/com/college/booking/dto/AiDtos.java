package com.college.booking.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public class AiDtos {

    public record InterpretRequest(@NotBlank String prompt) {
    }

    public record ChatRequest(@NotBlank String message, List<ChatTurn> history) {
    }

    public record ChatTurn(String role, String content) {
    }

    public record Intent(
            String resourceType,
            Integer capacity,
            String date,
            String startTime,
            String endTime,
            List<String> requiredFacilities,
            String building,
            String query,
            String intent
    ) {
    }

    public record Recommendation(
            Long resourceId,
            String resourceName,
            String resourceCode,
            int score,
            String reason,
            boolean available
    ) {
    }

    public record InterpretResponse(
            Intent intent,
            List<Recommendation> recommendations,
            String explanation,
            boolean aiAvailable
    ) {
    }

    public record ChatResponse(
            String reply,
            List<Map<String, Object>> data,
            boolean aiAvailable
    ) {
    }

    public record InsightsRequest(@NotBlank String question) {
    }
}
