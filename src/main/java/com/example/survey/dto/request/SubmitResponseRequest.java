package com.example.survey.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SubmitResponseRequest {
    @NotEmpty
    private List<AnswerRequest> answers;

    private LocalDateTime startedAt;
    private Integer timeTakenSeconds;

    @Data
    public static class AnswerRequest {
        private UUID questionId;
        private String textValue;
        private Double numericValue;
        private List<String> selectedOptions;
        private Boolean booleanValue;
        private String dateValue;
        private Object matrixAnswers;
    }
}
