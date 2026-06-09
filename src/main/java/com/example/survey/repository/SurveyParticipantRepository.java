package com.example.survey.repository;

import com.example.survey.entity.SurveyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyParticipantRepository extends JpaRepository<SurveyParticipant, UUID> {
    long countBySurveyId(UUID surveyId);
    Optional<SurveyParticipant> findBySurveyIdAndUserId(UUID surveyId, UUID userId);
    boolean existsBySurveyIdAndUserId(UUID surveyId, UUID userId);
}
