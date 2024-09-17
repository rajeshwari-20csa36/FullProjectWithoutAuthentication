package com.ust.service;

import com.ust.client.TimeZoneServiceClient;
import com.ust.model.Employee;
import com.ust.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TimeZoneServiceClient timeZoneServiceClient;
//    private final PasswordEncoder passwordEncoder;

//    @Autowired
//    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
//        this.employeeRepository = employeeRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
@Autowired
public EmployeeService(EmployeeRepository employeeRepository, TimeZoneServiceClient timeZoneServiceClient) {
    this.employeeRepository = employeeRepository;
    this.timeZoneServiceClient = timeZoneServiceClient;
}

    public List<ZonedDateTime> getTeamOverlappingHours(List<Long> employeeIds, LocalDate date) {
        return timeZoneServiceClient.getOverlappingWorkingHours(employeeIds, date);
    }

//    @Transactional(readOnly = true)
//    public List<Employee> getAllEmployees() {
//        return employeeRepository.findAll();
//    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployeesPaginated(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        employee.setPassword(employee.getPassword());
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setName(employeeDetails.getName());
                    if (!employee.getEmail().equals(employeeDetails.getEmail()) &&
                            employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists");
                    }
                    employee.setEmail(employeeDetails.getEmail());
                    employee.setLocation(employeeDetails.getLocation());
                    employee.setDesignation(employeeDetails.getDesignation());
                    employee.setRole(employeeDetails.getRole());
                    employee.setSkills(employeeDetails.getSkills());
                    employee.setTeamMember(employeeDetails.isTeamMember());
                    return employeeRepository.save(employee);
                })
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    @Transactional
    public Employee addSkillToEmployee(Long id, String skill) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.addSkill(skill);
                    return employeeRepository.save(employee);
                })
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional
    public Employee removeSkillFromEmployee(Long id, String skill) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.removeSkill(skill);
                    return employeeRepository.save(employee);
                })
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String searchTerm) {
        return employeeRepository.searchEmployees(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<Employee> findEmployeesBySkill(String skill) {
        return employeeRepository.findBySkill(skill);
    }

    @Transactional(readOnly = true)
    public long countEmployeesWithSkill(String skill) {
        return employeeRepository.countEmployeesWithSkill(skill);
    }

    @Transactional(readOnly = true)
    public List<Employee> getTeamMembers() {
        return employeeRepository.findByIsTeamMember(true);
    }

    @Transactional
    public Employee toggleTeamMembership(Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setTeamMember(!employee.isTeamMember());
                    return employeeRepository.save(employee);
                })
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Employee> findEmployees(String searchTerm, Set<String> skills, String location, Integer numberOfEmployees, Pageable pageable) {
        Specification<Employee> spec = Specification.where(null);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + searchTerm.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("designation")), "%" + searchTerm.toLowerCase() + "%"),
                            cb.like(cb.lower(root.join("skills")), "%" + searchTerm.toLowerCase() + "%")
                    )
            );
        }

        if (skills != null && !skills.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return root.join("skills").in(skills);
            });
        }

        if (location != null && !location.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("location")), location.toLowerCase())
            );
        }

        Page<Employee> results = employeeRepository.findAll(spec, pageable);

        if (numberOfEmployees != null && numberOfEmployees > 0) {
            if (results.getTotalElements() != numberOfEmployees) {
                return Page.empty(pageable);
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSkillsDistribution() {
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Long> distribution = new HashMap<>();
        for (Employee employee : employees) {
            for (String skill : employee.getSkills()) {
                distribution.put(skill, distribution.getOrDefault(skill, 0L) + 1);
            }
        }
        return distribution;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLocationDistribution() {
        return employeeRepository.findAll().stream()
                .collect(Collectors.groupingBy(Employee::getLocation, Collectors.counting()));
    }

    @Transactional
    public void updateEmployeePassword(Long id, String oldPassword, String newPassword) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
//        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
//            throw new RuntimeException("Old password is incorrect");
//        }
        employee.setPassword(newPassword);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<String> getAllUniqueSkills() {
        return employeeRepository.findAll().stream()
                .flatMap(e -> e.getSkills().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllUniqueLocations() {
        return employeeRepository.findAll().stream()
                .map(Employee::getLocation)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
