package com.example.survey.controller;

import com.example.survey.dto.request.CreateSurveyRequest;
import com.example.survey.dto.request.SubmitResponseRequest;
import com.example.survey.dto.response.ApiResponse;
import com.example.survey.dto.response.SurveyDto;
import com.example.survey.dto.response.SurveyStatsDto;
import com.example.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
@Tag(name = "Surveys", description = "Survey management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping
    @Operation(summary = "List surveys for organization")
    public ResponseEntity<ApiResponse<Page<SurveyDto>>> getSurveys(
        @RequestParam UUID orgId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            surveyService.getSurveysByOrganization(orgId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get survey by ID")
    public ResponseEntity<ApiResponse<SurveyDto>> getSurvey(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(surveyService.getSurveyById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new survey")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SurveyDto>> createSurvey(
        @Valid @RequestBody CreateSurveyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(surveyService.createSurvey(request)));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a draft survey")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SurveyDto>> publishSurvey(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(surveyService.publishSurvey(id)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close an active survey")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SurveyDto>> closeSurvey(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(surveyService.closeSurvey(id)));
    }

    @PostMapping("/{id}/responses")
    @Operation(summary = "Submit a survey response")
    public ResponseEntity<ApiResponse<UUID>> submitResponse(
        @PathVariable UUID id,
        @Valid @RequestBody SubmitResponseRequest request
    ) {
        UUID responseId = surveyService.submitResponse(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(responseId));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get survey statistics and analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public ResponseEntity<ApiResponse<SurveyStatsDto>> getSurveyStats(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(surveyService.getSurveyStats(id)));
    }
}
