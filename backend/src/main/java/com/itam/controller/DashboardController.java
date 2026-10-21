package com.itam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itam.model.dto.DashboardSummaryResponse;
import com.itam.service.dashboard.DashboardService;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/dashboard/summary")
    public DashboardSummaryResponse summary(
            @RequestParam(defaultValue = "30") int expiringSoonDays) {
        return dashboardService.getSummary(expiringSoonDays);
    }
}
