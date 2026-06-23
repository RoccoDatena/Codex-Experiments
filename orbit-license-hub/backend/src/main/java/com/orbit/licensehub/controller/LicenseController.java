package com.orbit.licensehub.controller;

import com.orbit.licensehub.dto.AssignLicenseRequest;
import com.orbit.licensehub.dto.LicenseResponse;
import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

  private final LicenseService licenseService;

  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @GetMapping
  public PageResponse<LicenseResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "id,desc") String sort,
      @RequestParam(required = false) Long tenantId) {
    return licenseService.list(page, size, search, sort, tenantId);
  }

  @PostMapping("/assign")
  public LicenseResponse assign(@Valid @RequestBody AssignLicenseRequest request) {
    return licenseService.assign(request);
  }

  @PostMapping("/{id}/deassign")
  public void deassign(@PathVariable Long id) {
    licenseService.deassign(id);
  }

  @PostMapping("/{id}/release")
  public void release(@PathVariable Long id) {
    licenseService.release(id);
  }
}
