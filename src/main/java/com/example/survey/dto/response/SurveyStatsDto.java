package com.example.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.survey.enums.SurveyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurveyStatsDto {
    private UUID surveyId;
    private String title;
    private SurveyStatus status;
    private long totalParticipants;
    private long totalResponses;
    private double responseRate;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private List<QuestionStat> questionStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStat {
        private UUID questionId;
        private String questionText;
        private String questionType;
        private long responseCount;
        private Map<String, Long> optionCounts;      // for select questions
        private Double averageRating;                 // for rating questions
        private Double npsScore;                      // for NPS questions
        private List<String> textResponses;           // for text questions (sample)
    }
}
