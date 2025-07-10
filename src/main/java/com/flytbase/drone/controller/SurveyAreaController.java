package com.flytbase.drone.controller;

import com.flytbase.drone.dto.surveyarea.CreateSurveyAreaRequest;
import com.flytbase.drone.dto.surveyarea.SurveyAreaResponse;
import com.flytbase.drone.dto.surveyarea.UpdateSurveyAreaRequest;
import com.flytbase.drone.service.SurveyAreaService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for survey area operations. */
@RestController
@RequestMapping("/api/survey-areas")
@CrossOrigin
@RequiredArgsConstructor
public class SurveyAreaController {

  private final SurveyAreaService surveyAreaService;

  /**
   * Get all survey areas.
   *
   * @return list of survey area responses
   */
  @GetMapping
  public ResponseEntity<List<SurveyAreaResponse>> getAllSurveyAreas() {
    return ResponseEntity.ok(surveyAreaService.getAllSurveyAreas());
  }

  /**
   * Get a survey area by ID.
   *
   * @param id the survey area ID
   * @return the survey area response
   */
  @GetMapping("/{id}")
  public ResponseEntity<SurveyAreaResponse> getSurveyAreaById(@PathVariable UUID id) {
    return ResponseEntity.ok(surveyAreaService.getSurveyAreaById(id));
  }

  /**
   * Create a new survey area.
   *
   * @param request the create survey area request
   * @return the created survey area response
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<SurveyAreaResponse> createSurveyArea(
      @Valid @RequestBody CreateSurveyAreaRequest request) {
    SurveyAreaResponse response = surveyAreaService.createSurveyArea(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing survey area.
   *
   * @param id the survey area ID
   * @param request the update survey area request
   * @return the updated survey area response
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<SurveyAreaResponse> updateSurveyArea(
      @PathVariable UUID id, @Valid @RequestBody UpdateSurveyAreaRequest request) {
    return ResponseEntity.ok(surveyAreaService.updateSurveyArea(id, request));
  }

  /**
   * Delete a survey area.
   *
   * @param id the survey area ID
   * @return no content response
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteSurveyArea(@PathVariable UUID id) {
    surveyAreaService.deleteSurveyArea(id);
    return ResponseEntity.noContent().build();
  }
}
