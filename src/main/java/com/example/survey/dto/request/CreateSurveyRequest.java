package com.example.survey.dto.request;

import com.example.survey.entity.Question;
import com.example.survey.enums.ParticipantScope;
import com.example.survey.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSurveyRequest {
    @NotBlank @Size(max = 300)
    private String title;

    @Size(max = 2000)
    private String description;

    private String welcomeMessage;
    private String thankYouMessage;

    @NotNull
    private ParticipantScope participantScope;

    @Valid
    private List<QuestionRequest> questions;

    private List<UUID> participantUserIds;
    private List<UUID> participantTeamIds;
    private List<UUID> participantDepartmentIds;

    private boolean isAnonymous = false;
    private boolean allowMultipleResponses = false;
    private boolean requireAllQuestions = false;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer timeLimitMinutes;
    private Integer maxResponses;

    @Data
    public static class QuestionRequest {
        @NotBlank
        private String questionText;

        private String description;

        @NotNull
        private QuestionType questionType;

        private boolean required = false;
        private List<Question.QuestionOption> options;
        private Integer minValue;
        private Integer maxValue;
        private String minLabel;
        private String maxLabel;
        private String placeholder;
        private List<String> matrixRows;
        private List<String> matrixColumns;
    }
}
