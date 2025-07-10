package com.flytbase.drone.service;

import com.flytbase.drone.dto.surveyarea.CreateSurveyAreaRequest;
import com.flytbase.drone.dto.surveyarea.SurveyAreaResponse;
import com.flytbase.drone.dto.surveyarea.UpdateSurveyAreaRequest;
import com.flytbase.drone.entity.Organization;
import com.flytbase.drone.entity.SurveyArea;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.SurveyAreaRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for survey area operations. */
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyAreaService {

  private final SurveyAreaRepository surveyAreaRepository;
  private final UserService userService;

  /**
   * Get all survey areas for the current user's organization.
   *
   * @return list of survey area responses
   */
  @Transactional(readOnly = true)
  public List<SurveyAreaResponse> getAllSurveyAreas() {
    User currentUser = userService.getCurrentUser();
    return surveyAreaRepository.findByOrganizationId(currentUser.getOrganization().getId()).stream()
        .map(SurveyAreaResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get a survey area by ID.
   *
   * @param id the survey area ID
   * @return the survey area response
   */
  @Transactional(readOnly = true)
  public SurveyAreaResponse getSurveyAreaById(UUID id) {
    User currentUser = userService.getCurrentUser();
    SurveyArea surveyArea =
        surveyAreaRepository
            .findById(id)
            .filter(
                sa -> sa.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Survey area not found with ID: " + id));
    return SurveyAreaResponse.fromEntity(surveyArea);
  }

  /**
   * Create a new survey area.
   *
   * @param request the create survey area request
   * @return the created survey area response
   */
  public SurveyAreaResponse createSurveyArea(CreateSurveyAreaRequest request) {
    User currentUser = userService.getCurrentUser();
    Organization organization = currentUser.getOrganization();

    // Check if name already exists in the organization
    if (surveyAreaRepository.existsByNameAndOrganizationId(
        request.getName(), organization.getId())) {
      throw new BusinessException(
          "Survey area with name " + request.getName() + " already exists in your organization");
    }

    SurveyArea surveyArea = new SurveyArea();
    surveyArea.setName(request.getName());
    surveyArea.setDescription(request.getDescription());
    surveyArea.setBoundaryPolygon(request.getBoundaryPolygon());
    surveyArea.setArea(request.getAreaSize());
    surveyArea.setOrganization(organization);
    surveyArea.setCreatedBy(currentUser);

    surveyArea = surveyAreaRepository.save(surveyArea);
    return SurveyAreaResponse.fromEntity(surveyArea);
  }

  /**
   * Update an existing survey area.
   *
   * @param id the survey area ID
   * @param request the update survey area request
   * @return the updated survey area response
   */
  public SurveyAreaResponse updateSurveyArea(UUID id, UpdateSurveyAreaRequest request) {
    User currentUser = userService.getCurrentUser();
    SurveyArea surveyArea =
        surveyAreaRepository
            .findById(id)
            .filter(
                sa -> sa.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Survey area not found with ID: " + id));

    // Check if name already exists in the organization (if name is being changed)
    if (request.getName() != null
        && !request.getName().equals(surveyArea.getName())
        && surveyAreaRepository.existsByNameAndOrganizationId(
            request.getName(), currentUser.getOrganization().getId())) {
      throw new BusinessException(
          "Survey area with name " + request.getName() + " already exists in your organization");
    }

    if (request.getName() != null) {
      surveyArea.setName(request.getName());
    }
    if (request.getDescription() != null) {
      surveyArea.setDescription(request.getDescription());
    }
    if (request.getBoundaryPolygon() != null) {
      surveyArea.setBoundaryPolygon(request.getBoundaryPolygon());
    }
    if (request.getAreaSize() != null) {
      surveyArea.setArea(request.getAreaSize());
    }

    surveyArea = surveyAreaRepository.save(surveyArea);
    return SurveyAreaResponse.fromEntity(surveyArea);
  }

  /**
   * Delete a survey area.
   *
   * @param id the survey area ID
   */
  public void deleteSurveyArea(UUID id) {
    User currentUser = userService.getCurrentUser();
    SurveyArea surveyArea =
        surveyAreaRepository
            .findById(id)
            .filter(
                sa -> sa.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Survey area not found with ID: " + id));

    surveyAreaRepository.delete(surveyArea);
  }
}
