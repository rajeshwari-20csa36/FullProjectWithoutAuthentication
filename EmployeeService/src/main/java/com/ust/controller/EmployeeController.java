package com.ust.controller;
import com.ust.model.Employee;
import com.ust.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")

public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @GetMapping("/team-overlap")
    public ResponseEntity<List<ZonedDateTime>> getTeamOverlappingHours(
            @RequestParam List<Long> employeeIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ZonedDateTime> overlappingHours = employeeService.getTeamOverlappingHours(employeeIds, date);
        return ResponseEntity.ok(overlappingHours);
    }

    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployeesPaginated(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        try {
            Employee newEmployee = employeeService.createEmployee(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        try {
            Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
            return ResponseEntity.ok(updatedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<Employee> addSkillToEmployee(@PathVariable Long id, @RequestBody String skill) {
        try {
            Employee updatedEmployee = employeeService.addSkillToEmployee(id, skill);
            return ResponseEntity.ok(updatedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/skills")
    public ResponseEntity<Employee> removeSkillFromEmployee(@PathVariable Long id, @RequestBody String skill) {
        try {
            Employee updatedEmployee = employeeService.removeSkillFromEmployee(id, skill);
            return ResponseEntity.ok(updatedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Employee>> searchEmployees(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Set<String> skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer numberOfEmployees,
            Pageable pageable) {
        Page<Employee> results = employeeService.findEmployees(searchTerm, skills, location, numberOfEmployees, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/skills/{skill}")
    public ResponseEntity<List<Employee>> findEmployeesBySkill(@PathVariable String skill) {
        List<Employee> employees = employeeService.findEmployeesBySkill(skill);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/skills/{skill}/count")
    public ResponseEntity<Long> countEmployeesWithSkill(@PathVariable String skill) {
        long count = employeeService.countEmployeesWithSkill(skill);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/team")
    public ResponseEntity<List<Employee>> getTeamMembers() {
        List<Employee> teamMembers = employeeService.getTeamMembers();
        return ResponseEntity.ok(teamMembers);
    }

    @PostMapping("/{id}/toggle-team")
    public ResponseEntity<Employee> toggleTeamMembership(@PathVariable Long id) {
        try {
            Employee updatedEmployee = employeeService.toggleTeamMembership(id);
            return ResponseEntity.ok(updatedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/skills/distribution")
    public ResponseEntity<Map<String, Long>> getSkillsDistribution() {
        Map<String, Long> distribution = employeeService.getSkillsDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/locations/distribution")
    public ResponseEntity<Map<String, Long>> getLocationDistribution() {
        Map<String, Long> distribution = employeeService.getLocationDistribution();
        return ResponseEntity.ok(distribution);
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> updateEmployeePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            employeeService.updateEmployeePassword(id, oldPassword, newPassword);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/skills")
    public ResponseEntity<List<String>> getAllUniqueSkills() {
        List<String> skills = employeeService.getAllUniqueSkills();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getAllUniqueLocations() {
        List<String> locations = employeeService.getAllUniqueLocations();
        return ResponseEntity.ok(locations);
    }
}


