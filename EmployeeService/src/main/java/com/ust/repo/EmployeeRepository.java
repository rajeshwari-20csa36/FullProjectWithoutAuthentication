package com.ust.repo;

import com.ust.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByIsTeamMember(boolean isTeamMember);

    @Query("SELECT e FROM Employee e JOIN e.skills s WHERE LOWER(s) = LOWER(:skill)")
    List<Employee> findBySkill(@Param("skill") String skill);

    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR EXISTS (SELECT 1 FROM e.skills s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Employee> searchEmployees(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(DISTINCT e) FROM Employee e JOIN e.skills s WHERE LOWER(s) = LOWER(:skill)")
    long countEmployeesWithSkill(@Param("skill") String skill);
}
