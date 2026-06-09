package com.example.survey.repository;

import com.example.survey.entity.Survey;
import com.example.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    Page<Survey> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Survey> findByOrganizationIdAndStatus(UUID organizationId, SurveyStatus status, Pageable pageable);

    @Query("SELECT s FROM Survey s WHERE s.organization.id = :orgId " +
           "AND s.createdBy.id = :userId")
    Page<Survey> findByOrganizationIdAndCreatedById(@Param("orgId") UUID orgId,
                                                     @Param("userId") UUID userId,
                                                     Pageable pageable);

    // Surveys assigned to a specific user (via participant table)
    @Query("SELECT DISTINCT s FROM Survey s " +
           "JOIN s.participants p " +
           "WHERE s.organization.id = :orgId " +
           "AND (p.user.id = :userId OR p.team.id = :teamId OR p.department.id = :deptId) " +
           "AND s.status = 'ACTIVE'")
    List<Survey> findActiveSurveysForUser(@Param("orgId") UUID orgId,
                                           @Param("userId") UUID userId,
                                           @Param("teamId") UUID teamId,
                                           @Param("deptId") UUID deptId);

    // Surveys expiring soon
    @Query("SELECT s FROM Survey s WHERE s.status = 'ACTIVE' " +
           "AND s.endDate BETWEEN :now AND :threshold")
    List<Survey> findSurveysExpiringSoon(@Param("now") LocalDateTime now,
                                          @Param("threshold") LocalDateTime threshold);

    // Count active surveys per organization
    @Query("SELECT COUNT(s) FROM Survey s WHERE s.organization.id = :orgId AND s.status = 'ACTIVE'")
    long countActiveSurveysByOrg(@Param("orgId") UUID orgId);

    // Analytics: response rate per survey
    @Query("SELECT s.id, s.title, " +
           "COUNT(DISTINCT sp.id) as invited, " +
           "COUNT(DISTINCT sr.id) as responded " +
           "FROM Survey s " +
           "LEFT JOIN s.participants sp " +
           "LEFT JOIN s.responses sr ON sr.isComplete = true " +
           "WHERE s.organization.id = :orgId " +
           "GROUP BY s.id, s.title")
    List<Object[]> getSurveyResponseRates(@Param("orgId") UUID orgId);

    @Query("SELECT s FROM Survey s WHERE s.organization.id = :orgId " +
           "AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Survey> searchByOrganization(@Param("orgId") UUID orgId,
                                       @Param("search") String search,
                                       Pageable pageable);
}
