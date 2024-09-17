package com.ust.service;

import com.ust.model.EmployeeTimeZone;
import com.ust.repo.EmployeeTimeZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;


@Service
@RequiredArgsConstructor
public class TimeZoneService {
    private final EmployeeTimeZoneRepository repository;

    public EmployeeTimeZone saveEmployeeTimeZone(EmployeeTimeZone employeeTimeZone) {
        return repository.save(employeeTimeZone);
    }

    public List<EmployeeTimeZone> getAllEmployeeTimeZones() {
        return repository.findAll();
    }

    public Optional<EmployeeTimeZone> getEmployeeTimeZoneById(Long employeeId) {
        return repository.findById(employeeId);
    }
    public void deleteEmployeeTimeZone(Long employeeId) {
        repository.deleteById(employeeId);
    }

    public boolean validateMeetingTime(List<Long> employeeIds, ZonedDateTime proposedMeetingTime) {
        List<EmployeeTimeZone> employeeTimeZones = repository.findAllById(employeeIds);
        return employeeTimeZones.stream().allMatch(etz -> {
            ZoneId employeeZone = ZoneId.of(etz.getTimeZone());
            ZonedDateTime employeeTime = proposedMeetingTime.withZoneSameInstant(employeeZone);
            return employeeTime.toLocalTime().isAfter(etz.getWorkingHoursStart()) &&
                    employeeTime.toLocalTime().isBefore(etz.getWorkingHoursEnd());
        });
    }

    public List<ZonedDateTime> calculateOverlappingWorkingHours(List<Long> employeeIds, LocalDate date) {
        List<EmployeeTimeZone> employeeTimeZones = repository.findAllById(employeeIds);

        // Convert working hours to ZonedDateTime for the given date
        List<Map.Entry<ZonedDateTime, ZonedDateTime>> workingHours = employeeTimeZones.stream()
                .map(etz -> {
                    ZoneId zoneId = ZoneId.of(etz.getTimeZone());
                    ZonedDateTime start = ZonedDateTime.of(date, etz.getWorkingHoursStart(), zoneId);
                    ZonedDateTime end = ZonedDateTime.of(date, etz.getWorkingHoursEnd(), zoneId);
                    return Map.entry(start, end);
                })
                .toList();

        // Find the latest start time and earliest end time
        ZonedDateTime latestStart = workingHours.stream()
                .map(Map.Entry::getKey)
                .max(ZonedDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No working hours found"));

        ZonedDateTime earliestEnd = workingHours.stream()
                .map(Map.Entry::getValue)
                .min(ZonedDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No working hours found"));

        // If there's no overlap, return an empty list
        if (latestStart.isAfter(earliestEnd)) {
            return Collections.emptyList();
        }

        // Generate 30-minute slots within the overlapping period
        List<ZonedDateTime> slots = new ArrayList<>();
        ZonedDateTime slotStart = latestStart;
        while (slotStart.isBefore(earliestEnd)) {
            slots.add(slotStart);
            slotStart = slotStart.plusMinutes(30);
        }

        return slots;
    }

    public ZonedDateTime suggestBestMeetingTime(List<Long> employeeIds, LocalDate startDate, int daysToCheck) {
        Map<ZonedDateTime, Integer> slotScores = new HashMap<>();

        for (int i = 0; i < daysToCheck; i++) {
            LocalDate date = startDate.plusDays(i);
            List<ZonedDateTime> overlappingSlots = calculateOverlappingWorkingHours(employeeIds, date);

            for (ZonedDateTime slot : overlappingSlots) {
                int score = calculateSlotScore(slot);
                slotScores.merge(slot, score, Integer::sum);
            }
        }

        return slotScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No suitable meeting time found"));
    }

    private int calculateSlotScore(ZonedDateTime slot) {
        int score = 10; // Base score

        // Prefer times closer to the middle of the working day
        LocalTime slotTime = slot.toLocalTime();
        LocalTime midDay = LocalTime.of(12, 0);
        long minutesFromMidDay = Math.abs(Duration.between(slotTime, midDay).toMinutes());
        score -= minutesFromMidDay / 30; // Decrease score by 1 for every 30 minutes away from midday

        // Avoid early morning and late evening slots
        if (slotTime.isBefore(LocalTime.of(9, 0)) || slotTime.isAfter(LocalTime.of(16, 0))) {
            score -= 5;
        }

        // Prefer monday, Tuesday, Wednesday, Thursday,friday
        DayOfWeek dayOfWeek = slot.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY ||dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY || dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.FRIDAY) {
            score += 3;
        }

        return Math.max(score, 0); // Ensure score is not negative
    }

    public Duration calculateFreeHours(EmployeeTimeZone etz) {
        LocalTime workEnd = etz.getWorkingHoursEnd();
        LocalTime nextDayWorkStart = etz.getWorkingHoursStart();

        if (workEnd.isAfter(nextDayWorkStart)) {
            return Duration.between(workEnd, LocalTime.MAX)
                    .plus(Duration.between(LocalTime.MIN, nextDayWorkStart));
        } else {
            return Duration.between(workEnd, nextDayWorkStart);
        }
    }

    public Map<String, Duration> getTeamFreeHoursOverlap(List<Long> employeeIds) {
        List<EmployeeTimeZone> teamMembers = repository.findAllById(employeeIds);
        Map<String, Duration> overlapMap = new HashMap<>();

        for (int i = 0; i < teamMembers.size(); i++) {
            for (int j = i + 1; j < teamMembers.size(); j++) {
                EmployeeTimeZone member1 = teamMembers.get(i);
                EmployeeTimeZone member2 = teamMembers.get(j);

                Duration overlap = calculateFreeHoursOverlap(member1, member2);
                String key = member1.getEmployeeId() + " & " + member2.getEmployeeId();
                overlapMap.put(key, overlap);
            }
        }

        return overlapMap;
    }

    private Duration calculateFreeHoursOverlap(EmployeeTimeZone etz1, EmployeeTimeZone etz2) {
        LocalDateTime start1 = LocalDateTime.of(LocalDate.now(), etz1.getWorkingHoursEnd());
        LocalDateTime end1 = LocalDateTime.of(LocalDate.now().plusDays(1), etz1.getWorkingHoursStart());
        LocalDateTime start2 = LocalDateTime.of(LocalDate.now(), etz2.getWorkingHoursEnd());
        LocalDateTime end2 = LocalDateTime.of(LocalDate.now().plusDays(1), etz2.getWorkingHoursStart());

        ZonedDateTime zonedStart1 = ZonedDateTime.of(start1, ZoneId.of(etz1.getTimeZone()));
        ZonedDateTime zonedEnd1 = ZonedDateTime.of(end1, ZoneId.of(etz1.getTimeZone()));
        ZonedDateTime zonedStart2 = ZonedDateTime.of(start2, ZoneId.of(etz2.getTimeZone()));
        ZonedDateTime zonedEnd2 = ZonedDateTime.of(end2, ZoneId.of(etz2.getTimeZone()));

        ZonedDateTime overlapStart = zonedStart1.isAfter(zonedStart2) ? zonedStart1 : zonedStart2;
        ZonedDateTime overlapEnd = zonedEnd1.isBefore(zonedEnd2) ? zonedEnd1 : zonedEnd2;

        if (overlapStart.isBefore(overlapEnd)) {
            return Duration.between(overlapStart, overlapEnd);
        } else {
            return Duration.ZERO;
        }
    }
}