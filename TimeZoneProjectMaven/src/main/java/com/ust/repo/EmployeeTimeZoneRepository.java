package com.ust.repo;

import com.ust.model.EmployeeTimeZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeTimeZoneRepository extends JpaRepository<EmployeeTimeZone, Long> {
}