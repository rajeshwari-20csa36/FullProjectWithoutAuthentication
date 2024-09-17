package com.ust.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@FeignClient(name = "timezone-service")
public interface TimeZoneServiceClient {
    @GetMapping("/api/timezone/overlap")
    List<ZonedDateTime> getOverlappingWorkingHours(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}