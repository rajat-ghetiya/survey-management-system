package com.example.survey.repository;

import com.example.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {
    boolean existsBySurveyIdAndRespondentIdAndIsCompleteTrue(UUID surveyId, UUID respondentId);
    long countBySurveyIdAndIsCompleteTrue(UUID surveyId);

    @Query("SELECT AVG(sr.timeTakenSeconds) FROM SurveyResponse sr WHERE sr.survey.id = :surveyId AND sr.isComplete = true")
    Double avgCompletionTimeBySurveyId(@Param("surveyId") UUID surveyId);
}
