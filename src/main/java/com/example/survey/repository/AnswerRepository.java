package com.example.survey.repository;

import com.example.survey.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId")
    List<Answer> findByQuestionId(@Param("questionId") UUID questionId);

    @Query("SELECT a FROM Answer a " +
           "JOIN a.surveyResponse sr " +
           "WHERE sr.survey.id = :surveyId AND a.question.id = :questionId " +
           "AND sr.isComplete = true")
    List<Answer> findCompletedAnswersByQuestionId(@Param("surveyId") UUID surveyId,
                                                   @Param("questionId") UUID questionId);

    @Query("SELECT AVG(a.numericValue) FROM Answer a " +
           "JOIN a.surveyResponse sr " +
           "WHERE a.question.id = :questionId AND sr.isComplete = true " +
           "AND a.numericValue IS NOT NULL")
    Double avgNumericValueByQuestionId(@Param("questionId") UUID questionId);
}
