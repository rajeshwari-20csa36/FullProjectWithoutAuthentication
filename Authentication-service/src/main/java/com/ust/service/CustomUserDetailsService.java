package com.ust.service;

import com.ust.client.EmployeeClient;
import com.ust.dto.EmployeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeClient employeeClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Load user details from Employee Service using Feign client
        EmployeeDto employee = employeeClient.getEmployeeByEmail(email);
        if (employee == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // Convert EmployeeDto to UserDetails
        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword())
                .roles(employee.getRole())
                .build();
    }
}