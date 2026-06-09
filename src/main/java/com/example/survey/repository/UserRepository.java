package com.example.survey.repository;

import com.example.survey.entity.User;
import com.example.survey.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.isActive = true")
    Page<User> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.role = :role AND u.isActive = true")
    List<User> findByOrganizationIdAndRole(@Param("orgId") UUID orgId, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.team.id = :teamId AND u.isActive = true")
    List<User> findByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT u FROM User u WHERE u.department.id = :deptId AND u.isActive = true")
    List<User> findByDepartmentId(@Param("deptId") UUID deptId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND u.isActive = true")
    long countByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.isActive = true")
    Page<User> searchByOrganization(@Param("orgId") UUID orgId,
                                     @Param("search") String search,
                                     Pageable pageable);
}
