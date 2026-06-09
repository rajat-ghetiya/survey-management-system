package com.example.survey.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"survey_id", "user_id"}),
    @UniqueConstraint(columnNames = {"survey_id", "team_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // Individual participant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Team participant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // Department participant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "has_responded", nullable = false)
    @Builder.Default
    private boolean hasResponded = false;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "reminded_at")
    private LocalDateTime remindedAt;

    @Column(name = "invited_by_id")
    private UUID invitedById;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
