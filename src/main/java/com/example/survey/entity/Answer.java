package com.example.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "answers", indexes = {
    @Index(name = "idx_answers_response", columnList = "survey_response_id"),
    @Index(name = "idx_answers_question", columnList = "question_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Text answer
    @Column(name = "text_value", columnDefinition = "NVARCHAR(MAX)")
    private String textValue;

    // Numeric answer (rating, NPS, slider)
    @Column(name = "numeric_value")
    private Double numericValue;

    // Multi-select / ranking stored as JSON array of option IDs
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_options", columnDefinition = "NVARCHAR(MAX)")
    private List<String> selectedOptions;

    // Matrix answers stored as JSON { "rowId": "columnId" }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_answers", columnDefinition = "NVARCHAR(MAX)")
    private Object matrixAnswers;

    // File upload reference
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "date_value")
    private String dateValue;

    @Column(name = "date_range_start")
    private String dateRangeStart;

    @Column(name = "date_range_end")
    private String dateRangeEnd;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
