package com.example.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.survey.enums.ParticipantScope;
import com.example.survey.enums.SurveyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurveyDto {
    private UUID id;
    private String title;
    private String description;
    private SurveyStatus status;
    private ParticipantScope participantScope;
    private int questionCount;
    private String createdByName;
    private UUID createdById;
    private boolean isAnonymous;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
