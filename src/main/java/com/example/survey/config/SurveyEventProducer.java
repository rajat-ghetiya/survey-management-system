package com.example.survey.config;

import com.example.survey.entity.Survey;
import com.example.survey.entity.SurveyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.survey-events}")
    private String surveyEventsTopic;

    @Value("${app.kafka.topics.response-events}")
    private String responseEventsTopic;

    @Value("${app.kafka.topics.notification-events}")
    private String notificationEventsTopic;

    @Value("${app.kafka.topics.audit-events}")
    private String auditEventsTopic;

    public void publishSurveyCreated(Survey survey) {
        Map<String, Object> event = buildSurveyEvent("SURVEY_CREATED", survey);
        sendEvent(surveyEventsTopic, survey.getId().toString(), event);
    }

    public void publishSurveyPublished(Survey survey) {
        Map<String, Object> event = buildSurveyEvent("SURVEY_PUBLISHED", survey);
        sendEvent(surveyEventsTopic, survey.getId().toString(), event);
        sendNotification(survey, "SURVEY_PUBLISHED_NOTIFICATION");
    }

    public void publishSurveyClosed(Survey survey) {
        Map<String, Object> event = buildSurveyEvent("SURVEY_CLOSED", survey);
        sendEvent(surveyEventsTopic, survey.getId().toString(), event);
    }

    public void publishResponseSubmitted(SurveyResponse response) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "RESPONSE_SUBMITTED");
        event.put("responseId", response.getId().toString());
        event.put("surveyId", response.getSurvey().getId().toString());
        event.put("respondentId", response.getRespondent() != null
            ? response.getRespondent().getId().toString() : "ANONYMOUS");
        event.put("submittedAt", LocalDateTime.now().toString());
        sendEvent(responseEventsTopic, response.getSurvey().getId().toString(), event);
    }

    public void publishAuditEvent(String action, String entityType, String entityId,
                                   String actorEmail, String actorRole, String result) {
        Map<String, Object> event = new HashMap<>();
        event.put("action", action);
        event.put("entityType", entityType);
        event.put("entityId", entityId);
        event.put("actorEmail", actorEmail);
        event.put("actorRole", actorRole);
        event.put("result", result);
        event.put("timestamp", LocalDateTime.now().toString());
        sendEvent(auditEventsTopic, action + "_" + entityId, event);
    }

    private Map<String, Object> buildSurveyEvent(String eventType, Survey survey) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("surveyId", survey.getId().toString());
        event.put("title", survey.getTitle());
        event.put("organizationId", survey.getOrganization().getId().toString());
        event.put("createdById", survey.getCreatedBy().getId().toString());
        event.put("status", survey.getStatus().name());
        event.put("timestamp", LocalDateTime.now().toString());
        return event;
    }

    private void sendNotification(Survey survey, String notificationType) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", notificationType);
        notification.put("surveyId", survey.getId().toString());
        notification.put("surveyTitle", survey.getTitle());
        notification.put("organizationId", survey.getOrganization().getId().toString());
        notification.put("participantScope", survey.getParticipantScope().name());
        sendEvent(notificationEventsTopic, survey.getId().toString(), notification);
    }

    private void sendEvent(String topic, String key, Map<String, Object> payload) {
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event to topic {}: {}", topic, ex.getMessage());
            } else {
                log.debug("Event sent to topic {}, partition {}, offset {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
