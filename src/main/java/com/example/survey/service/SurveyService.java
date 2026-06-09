package com.example.survey.service;

import com.example.survey.dto.request.CreateSurveyRequest;
import com.example.survey.dto.request.SubmitResponseRequest;
import com.example.survey.dto.response.SurveyDto;
import com.example.survey.dto.response.SurveyStatsDto;
import com.example.survey.entity.*;
import com.example.survey.enums.ParticipantScope;
import com.example.survey.enums.Role;
import com.example.survey.enums.SurveyStatus;
import com.example.survey.exception.AccessDeniedException;
import com.example.survey.exception.ResourceNotFoundException;
import com.example.survey.config.SurveyEventProducer;
import com.example.survey.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyParticipantRepository participantRepository;
    private final AnswerRepository answerRepository;
    private final SurveyEventProducer eventProducer;

    @Transactional(readOnly = true)
    @Cacheable(value = "surveys", key = "#orgId + '_' + #pageable.pageNumber")
    public Page<SurveyDto> getSurveysByOrganization(UUID orgId, Pageable pageable) {
        return surveyRepository.findByOrganizationId(orgId, pageable)
            .map(this::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "surveys", key = "#surveyId")
    public SurveyDto getSurveyById(UUID surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey", surveyId));
        validateAccess(survey);
        return toDto(survey);
    }

    @CacheEvict(value = "surveys", allEntries = true)
    public SurveyDto createSurvey(CreateSurveyRequest request) {
        User currentUser = getCurrentUser();
        validateCanManageSurveys(currentUser);

        Survey survey = Survey.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .welcomeMessage(request.getWelcomeMessage())
            .thankYouMessage(request.getThankYouMessage())
            .status(SurveyStatus.DRAFT)
            .participantScope(request.getParticipantScope())
            .organization(currentUser.getOrganization())
            .createdBy(currentUser)
            .isAnonymous(request.isAnonymous())
            .allowMultipleResponses(request.isAllowMultipleResponses())
            .requireAllQuestions(request.isRequireAllQuestions())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .timeLimitMinutes(request.getTimeLimitMinutes())
            .maxResponses(request.getMaxResponses())
            .build();

        // Add questions
        if (request.getQuestions() != null) {
            for (int i = 0; i < request.getQuestions().size(); i++) {
                var qReq = request.getQuestions().get(i);
                Question question = Question.builder()
                    .survey(survey)
                    .questionText(qReq.getQuestionText())
                    .description(qReq.getDescription())
                    .questionType(qReq.getQuestionType())
                    .position(i + 1)
                    .isRequired(qReq.isRequired())
                    .options(qReq.getOptions())
                    .minValue(qReq.getMinValue())
                    .maxValue(qReq.getMaxValue())
                    .minLabel(qReq.getMinLabel())
                    .maxLabel(qReq.getMaxLabel())
                    .placeholder(qReq.getPlaceholder())
                    .build();
                survey.getQuestions().add(question);
            }
        }

        Survey saved = surveyRepository.save(survey);
        addParticipants(saved, request);

        eventProducer.publishSurveyCreated(saved);
        log.info("Survey created: {} by user: {}", saved.getId(), currentUser.getEmail());
        return toDto(saved);
    }

    @CacheEvict(value = "surveys", allEntries = true)
    public SurveyDto publishSurvey(UUID surveyId) {
        Survey survey = getSurveyEntity(surveyId);
        User currentUser = getCurrentUser();

        validateSurveyOwnerOrManager(survey, currentUser);

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT surveys can be published");
        }
        if (survey.getQuestions().isEmpty()) {
            throw new IllegalStateException("Survey must have at least one question");
        }

        survey.setStatus(SurveyStatus.ACTIVE);
        survey.setPublishedAt(LocalDateTime.now());

        Survey saved = surveyRepository.save(survey);
        eventProducer.publishSurveyPublished(saved);

        return toDto(saved);
    }

    @CacheEvict(value = "surveys", allEntries = true)
    public SurveyDto closeSurvey(UUID surveyId) {
        Survey survey = getSurveyEntity(surveyId);
        User currentUser = getCurrentUser();

        validateSurveyOwnerOrManager(survey, currentUser);

        survey.setStatus(SurveyStatus.CLOSED);
        survey.setClosedAt(LocalDateTime.now());

        Survey saved = surveyRepository.save(survey);
        eventProducer.publishSurveyClosed(saved);

        return toDto(saved);
    }

    @Transactional
    public UUID submitResponse(UUID surveyId, SubmitResponseRequest request) {
        Survey survey = getSurveyEntity(surveyId);
        User currentUser = getCurrentUser();

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Survey is not currently active");
        }

        // Check if user already responded (if not allowing multiple)
        if (!survey.isAllowMultipleResponses()) {
            boolean alreadyResponded = responseRepository
                .existsBySurveyIdAndRespondentIdAndIsCompleteTrue(surveyId, currentUser.getId());
            if (alreadyResponded) {
                throw new IllegalStateException("You have already submitted a response for this survey");
            }
        }

        SurveyResponse response = SurveyResponse.builder()
            .survey(survey)
            .respondent(survey.isAnonymous() ? null : currentUser)
            .isComplete(true)
            .completionPercentage(100)
            .startedAt(request.getStartedAt())
            .submittedAt(LocalDateTime.now())
            .timeTakenSeconds(request.getTimeTakenSeconds())
            .build();

        // Build answers
        Map<UUID, Question> questionMap = new HashMap<>();
        survey.getQuestions().forEach(q -> questionMap.put(q.getId(), q));

        for (var answerReq : request.getAnswers()) {
            Question question = questionMap.get(answerReq.getQuestionId());
            if (question == null) continue;

            Answer answer = Answer.builder()
                .surveyResponse(response)
                .question(question)
                .textValue(answerReq.getTextValue())
                .numericValue(answerReq.getNumericValue())
                .selectedOptions(answerReq.getSelectedOptions())
                .booleanValue(answerReq.getBooleanValue())
                .dateValue(answerReq.getDateValue())
                .build();
            response.getAnswers().add(answer);
        }

        SurveyResponse saved = responseRepository.save(response);

        // Mark participant as responded
        participantRepository.findBySurveyIdAndUserId(surveyId, currentUser.getId())
            .ifPresent(p -> {
                p.setHasResponded(true);
                participantRepository.save(p);
            });

        eventProducer.publishResponseSubmitted(saved);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "survey-stats", key = "#surveyId")
    public SurveyStatsDto getSurveyStats(UUID surveyId) {
        Survey survey = getSurveyEntity(surveyId);
        User currentUser = getCurrentUser();
        validateSurveyOwnerOrManager(survey, currentUser);

        long totalParticipants = participantRepository.countBySurveyId(surveyId);
        long totalResponses = responseRepository.countBySurveyIdAndIsCompleteTrue(surveyId);
        double responseRate = totalParticipants > 0
            ? (double) totalResponses / totalParticipants * 100 : 0;

        return SurveyStatsDto.builder()
            .surveyId(surveyId)
            .title(survey.getTitle())
            .status(survey.getStatus())
            .totalParticipants(totalParticipants)
            .totalResponses(totalResponses)
            .responseRate(Math.round(responseRate * 100.0) / 100.0)
            .publishedAt(survey.getPublishedAt())
            .closedAt(survey.getClosedAt())
            .build();
    }

    private void addParticipants(Survey survey, CreateSurveyRequest request) {
        if (request.getParticipantScope() == ParticipantScope.INDIVIDUAL && request.getParticipantUserIds() != null) {
            request.getParticipantUserIds().forEach(userId -> {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    SurveyParticipant participant = SurveyParticipant.builder()
                        .survey(survey)
                        .user(user)
                        .build();
                    survey.getParticipants().add(participant);
                }
            });
        }
        surveyRepository.save(survey);
    }

    private void validateSurveyOwnerOrManager(Survey survey, User user) {
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN) return;
        if (survey.getCreatedBy().getId().equals(user.getId())) return;
        if (survey.getManagers().stream().anyMatch(m -> m.getId().equals(user.getId()))) return;
        throw new AccessDeniedException("You do not have permission to manage this survey");
    }

    private void validateCanManageSurveys(User user) {
        if (user.getRole() == Role.USER) {
            throw new AccessDeniedException("Users cannot create surveys");
        }
    }

    private void validateAccess(Survey survey) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.SUPER_ADMIN) return;
        if (!survey.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to this survey");
        }
    }

    private Survey getSurveyEntity(UUID id) {
        return surveyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Survey", id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private SurveyDto toDto(Survey survey) {
        return SurveyDto.builder()
            .id(survey.getId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .status(survey.getStatus())
            .participantScope(survey.getParticipantScope())
            .questionCount(survey.getQuestions().size())
            .createdByName(survey.getCreatedBy().getFullName())
            .createdById(survey.getCreatedBy().getId())
            .isAnonymous(survey.isAnonymous())
            .startDate(survey.getStartDate())
            .endDate(survey.getEndDate())
            .publishedAt(survey.getPublishedAt())
            .createdAt(survey.getCreatedAt())
            .build();
    }
}
