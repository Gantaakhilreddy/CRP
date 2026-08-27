package com.college.booking.controller;

import com.college.booking.security.SecurityUtils;
import com.college.booking.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.forUser(SecurityUtils.currentUser());
    }

    @GetMapping("/analytics/heatmap")
    public List<Map<String, Object>> heatmap() {
        return dashboardService.heatmap();
    }

    @GetMapping("/analytics/utilization")
    public List<Map<String, Object>> utilization(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return dashboardService.utilization(from, to);
    }

    @GetMapping("/analytics/live")
    public Map<String, Object> live() {
        return dashboardService.liveCampus();
    }
}
