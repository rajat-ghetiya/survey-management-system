package com.example.survey.entity;


import com.example.survey.enums.SurveyStatus;
import com.example.survey.enums.ParticipantScope;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "surveys", indexes = {
    @Index(name = "idx_surveys_org", columnList = "organization_id"),
    @Index(name = "idx_surveys_status", columnList = "status"),
    @Index(name = "idx_surveys_created_by", columnList = "created_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "welcome_message", length = 1000)
    private String welcomeMessage;

    @Column(name = "thank_you_message", length = 1000)
    private String thankYouMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SurveyStatus status = SurveyStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_scope", nullable = false, length = 30)
    @Builder.Default
    private ParticipantScope participantScope = ParticipantScope.ENTIRE_ORGANIZATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Survey creator / owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Who was assigned to manage this survey (can be different from creator)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "survey_managers",
        joinColumns = @JoinColumn(name = "survey_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> managers = new HashSet<>();

    // Questions ordered by position
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // Survey participants (when scope = INDIVIDUAL or TEAM)
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SurveyParticipant> participants = new HashSet<>();

    // Responses submitted
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SurveyResponse> responses = new ArrayList<>();

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private boolean isAnonymous = false;

    @Column(name = "allow_multiple_responses", nullable = false)
    @Builder.Default
    private boolean allowMultipleResponses = false;

    @Column(name = "require_all_questions", nullable = false)
    @Builder.Default
    private boolean requireAllQuestions = false;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "max_responses")
    private Integer maxResponses;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
