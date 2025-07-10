package com.flytbase.drone.service;

import com.flytbase.drone.dto.report.CreateSurveyReportRequest;
import com.flytbase.drone.dto.report.SurveyReportResponse;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.SurveyReport;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.MissionRepository;
import com.flytbase.drone.repository.SurveyReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling survey report operations. */
@Service
public class SurveyReportService {

  private final SurveyReportRepository surveyReportRepository;
  private final MissionRepository missionRepository;
  private final UserService userService;

  @Autowired
  public SurveyReportService(
      SurveyReportRepository surveyReportRepository,
      MissionRepository missionRepository,
      UserService userService) {
    this.surveyReportRepository = surveyReportRepository;
    this.missionRepository = missionRepository;
    this.userService = userService;
  }

  /**
   * Create a new survey report.
   *
   * @param request the create survey report request
   * @return the created survey report response
   */
  @Transactional
  public SurveyReportResponse createSurveyReport(CreateSurveyReportRequest request) {
    // Validate mission
    Mission mission =
        missionRepository
            .findById(request.getMissionId())
            .orElseThrow(
                () ->
                    new BusinessException("Mission not found with ID: " + request.getMissionId()));

    // Verify user has access to the mission's organization
    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    // Create survey report
    SurveyReport report = new SurveyReport();
    report.setMission(mission);
    report.setGeneratedAt(
        request.getGeneratedAt() != null ? request.getGeneratedAt() : LocalDateTime.now());
    report.setReportType(request.getReportType());
    report.setTotalArea(request.getTotalArea());
    report.setCoveredArea(request.getCoveredArea());
    report.setImageCount(request.getImageCount());
    report.setAnomaliesDetected(request.getAnomaliesDetected());
    report.setReportData(request.getReportData());

    report = surveyReportRepository.save(report);
    return SurveyReportResponse.fromEntity(report);
  }

  /**
   * Get all survey reports for the current user's organization.
   *
   * @return list of survey report responses
   */
  public List<SurveyReportResponse> getAllSurveyReports() {
    Long organizationId = userService.getCurrentUser().getOrganization().getId();
    return surveyReportRepository
        .findByMission_Organization_IdOrderByGeneratedAtDesc(organizationId)
        .stream()
        .map(SurveyReportResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get a survey report by ID.
   *
   * @param id the survey report ID
   * @return the survey report response
   */
  public SurveyReportResponse getSurveyReportById(UUID id) {
    Long organizationId = userService.getCurrentUser().getOrganization().getId();
    SurveyReport report =
        surveyReportRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Survey report not found with ID: " + id));

    // Verify user has access to the report's organization
    if (!report.getMission().getOrganization().getId().equals(organizationId)) {
      throw new BusinessException("User does not have access to this survey report");
    }

    return SurveyReportResponse.fromEntity(report);
  }

  /**
   * Get all survey reports for a mission.
   *
   * @param missionId the mission ID
   * @return list of survey report responses
   */
  public List<SurveyReportResponse> getSurveyReportsByMission(UUID missionId) {
    // Verify mission exists and user has access
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    return surveyReportRepository.findByMissionIdOrderByGeneratedAtDesc(missionId).stream()
        .map(SurveyReportResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Generate a PDF report for a mission. This is a placeholder method that would be implemented
   * with a PDF generation library.
   *
   * @param missionId the mission ID
   * @return the survey report response
   */
  @Transactional
  public SurveyReportResponse generatePdfReport(UUID missionId) {
    // Verify mission exists and user has access
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    // In a real implementation, this would generate a PDF report
    // For now, we'll create a placeholder report
    CreateSurveyReportRequest request = new CreateSurveyReportRequest();
    request.setMissionId(missionId);
    request.setReportType("PDF");
    request.setTotalArea(1000.0);
    request.setCoveredArea(800.0);
    request.setImageCount(50);
    request.setAnomaliesDetected(5);
    request.setReportData("PDF report data would be stored here");

    return createSurveyReport(request);
  }

  /**
   * Generate an Excel report for a mission. This is a placeholder method that would be implemented
   * with an Excel generation library.
   *
   * @param missionId the mission ID
   * @return the survey report response
   */
  @Transactional
  public SurveyReportResponse generateExcelReport(UUID missionId) {
    // Verify mission exists and user has access
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    // In a real implementation, this would generate an Excel report
    // For now, we'll create a placeholder report
    CreateSurveyReportRequest request = new CreateSurveyReportRequest();
    request.setMissionId(missionId);
    request.setReportType("EXCEL");
    request.setTotalArea(1000.0);
    request.setCoveredArea(800.0);
    request.setImageCount(50);
    request.setAnomaliesDetected(5);
    request.setReportData("Excel report data would be stored here");

    return createSurveyReport(request);
  }
}
