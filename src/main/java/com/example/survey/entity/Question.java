package com.example.survey.entity;

import com.example.survey.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean isRequired = false;

    // JSON array of options for SINGLE_SELECT, MULTI_SELECT, DROPDOWN, RANKING, ELECTION
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "NVARCHAR(MAX)")
    private List<QuestionOption> options;

    // For RATING: min/max values and labels
    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(name = "min_label", length = 100)
    private String minLabel;

    @Column(name = "max_label", length = 100)
    private String maxLabel;

    // For MATRIX: rows and columns
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_rows", columnDefinition = "NVARCHAR(MAX)")
    private List<String> matrixRows;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_columns", columnDefinition = "NVARCHAR(MAX)")
    private List<String> matrixColumns;

    // Validation rules
    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "placeholder", length = 500)
    private String placeholder;

    @Column(name = "section_title", length = 200)
    private String sectionTitle;

    @Column(name = "show_if_question_id")
    private UUID showIfQuestionId;

    @Column(name = "show_if_answer_value", length = 500)
    private String showIfAnswerValue;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOption {
        private String id;
        private String label;
        private String value;
        private Integer order;
        private boolean isOther;
    }
}
