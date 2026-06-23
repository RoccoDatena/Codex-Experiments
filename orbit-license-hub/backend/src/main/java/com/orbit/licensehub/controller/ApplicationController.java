package com.orbit.licensehub.controller;

import com.orbit.licensehub.dto.ApplicationCredentialsResponse;
import com.orbit.licensehub.dto.ApplicationRequest;
import com.orbit.licensehub.dto.ApplicationResponse;
import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

  private final ApplicationService applicationService;

  public ApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @GetMapping
  public PageResponse<ApplicationResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "name,asc") String sort,
      @RequestParam(required = false) Long tenantId) {
    return applicationService.list(page, size, search, sort, tenantId);
  }

  @PostMapping
  public ApplicationCredentialsResponse create(@Valid @RequestBody ApplicationRequest request) {
    return applicationService.create(request);
  }

  @PutMapping("/{id}")
  public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
    return applicationService.update(id, request);
  }

  @PostMapping("/{id}/disable")
  public void disable(@PathVariable Long id) {
    applicationService.disable(id);
  }

  @PostMapping("/{id}/enable")
  public void enable(@PathVariable Long id) {
    applicationService.enable(id);
  }

  @GetMapping("/{id}/credentials")
  public ApplicationCredentialsResponse credentials(@PathVariable Long id) {
    return applicationService.credentials(id);
  }
}
