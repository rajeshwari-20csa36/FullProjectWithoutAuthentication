package com.ust.client;

import com.ust.dto.EmployeeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "employee-service")
public interface EmployeeClient {
    @GetMapping("/api/employees/email/{email}")
    EmployeeDto getEmployeeByEmail(@PathVariable String email);
    @GetMapping("/api/employees/{id}")
    EmployeeDto getEmployeeById(Long employeeId);
    @PostMapping("/api/employees/{id}/change-password")
    void updateEmployeePassword(Long employeeId, String encodedPassword);
}
