package com.flytbase.drone.controller;

import com.flytbase.drone.dto.report.CreateSurveyReportRequest;
import com.flytbase.drone.dto.report.SurveyReportResponse;
import com.flytbase.drone.service.SurveyReportService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for survey report operations. */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class SurveyReportController {

  private final SurveyReportService surveyReportService;

  @Autowired
  public SurveyReportController(SurveyReportService surveyReportService) {
    this.surveyReportService = surveyReportService;
  }

  /**
   * Get all survey reports.
   *
   * @return list of survey report responses
   */
  @GetMapping
  public ResponseEntity<List<SurveyReportResponse>> getAllSurveyReports() {
    return ResponseEntity.ok(surveyReportService.getAllSurveyReports());
  }

  /**
   * Get a survey report by ID.
   *
   * @param id the survey report ID
   * @return the survey report response
   */
  @GetMapping("/{id}")
  public ResponseEntity<SurveyReportResponse> getSurveyReportById(@PathVariable UUID id) {
    return ResponseEntity.ok(surveyReportService.getSurveyReportById(id));
  }

  /**
   * Get all survey reports for a mission.
   *
   * @param missionId the mission ID
   * @return list of survey report responses
   */
  @GetMapping("/mission/{missionId}")
  public ResponseEntity<List<SurveyReportResponse>> getSurveyReportsByMission(
      @PathVariable UUID missionId) {
    return ResponseEntity.ok(surveyReportService.getSurveyReportsByMission(missionId));
  }

  /**
   * Create a new survey report.
   *
   * @param request the create survey report request
   * @return the created survey report response
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<SurveyReportResponse> createSurveyReport(
      @Valid @RequestBody CreateSurveyReportRequest request) {
    SurveyReportResponse response = surveyReportService.createSurveyReport(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Generate a PDF report for a mission.
   *
   * @param missionId the mission ID
   * @return the survey report response
   */
  @PostMapping("/mission/{missionId}/pdf")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<SurveyReportResponse> generatePdfReport(@PathVariable UUID missionId) {
    SurveyReportResponse response = surveyReportService.generatePdfReport(missionId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Generate an Excel report for a mission.
   *
   * @param missionId the mission ID
   * @return the survey report response
   */
  @PostMapping("/mission/{missionId}/excel")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<SurveyReportResponse> generateExcelReport(@PathVariable UUID missionId) {
    SurveyReportResponse response = surveyReportService.generateExcelReport(missionId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
