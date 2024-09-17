package com.ust.controller;

import com.ust.model.EmployeeTimeZone;
import com.ust.service.TimeZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timezone")
@RequiredArgsConstructor
public class TimeZoneController {
    private final TimeZoneService timeZoneService;

    @PostMapping
    public ResponseEntity<EmployeeTimeZone> saveEmployeeTimeZone(@RequestBody EmployeeTimeZone employeeTimeZone) {
        return ResponseEntity.ok(timeZoneService.saveEmployeeTimeZone(employeeTimeZone));
    }



    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeTimeZone> getEmployeeTimeZone(@PathVariable Long employeeId) {
        return timeZoneService.getEmployeeTimeZoneById(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeTimeZone>> getAllEmployeeTimeZones() {
        return ResponseEntity.ok(timeZoneService.getAllEmployeeTimeZones());
    }

    @GetMapping("/overlap")
    public ResponseEntity<List<ZonedDateTime>> getOverlappingWorkingHours(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeZoneService.calculateOverlappingWorkingHours(employeeIds, date));
    }

    @GetMapping("/suggest-meeting")
    public ResponseEntity<ZonedDateTime> suggestBestMeetingTime(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "5") int daysToCheck) {
        return ResponseEntity.ok(timeZoneService.suggestBestMeetingTime(employeeIds, startDate, daysToCheck));
    }

    @GetMapping("/{employeeId}/free-hours")
    public ResponseEntity<Duration> getEmployeeFreeHours(@PathVariable Long employeeId) {
        return timeZoneService.getEmployeeTimeZoneById(employeeId)
                .map(etz -> ResponseEntity.ok(timeZoneService.calculateFreeHours(etz)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team-free-hours-overlap")
    public ResponseEntity<Map<String, Duration>> getTeamFreeHoursOverlap(@RequestParam List<Long> employeeIds) {
        return ResponseEntity.ok(timeZoneService.getTeamFreeHoursOverlap(employeeIds));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeTimeZone> updateEmployeeTimeZone(
            @PathVariable Long employeeId,
            @RequestBody EmployeeTimeZone updatedTimeZone) {
        return timeZoneService.getEmployeeTimeZoneById(employeeId)
                .map(existingTimeZone -> {
                    updatedTimeZone.setEmployeeId(employeeId);
                    return ResponseEntity.ok(timeZoneService.saveEmployeeTimeZone(updatedTimeZone));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployeeTimeZone(@PathVariable Long employeeId) {
        return timeZoneService.getEmployeeTimeZoneById(employeeId)
                .map(timeZone -> {
                    timeZoneService.deleteEmployeeTimeZone(employeeId);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validate-meeting-time")
    public ResponseEntity<Boolean> validateMeetingTime(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime proposedMeetingTime) {
        boolean isValid = timeZoneService.validateMeetingTime(employeeIds, proposedMeetingTime);
        return ResponseEntity.ok(isValid);
    }
}