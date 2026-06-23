package com.orbit.licensehub.controller;

import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.dto.TenantRequest;
import com.orbit.licensehub.dto.TenantResponse;
import com.orbit.licensehub.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

  private final TenantService tenantService;

  public TenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @GetMapping
  public PageResponse<TenantResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "name,asc") String sort) {
    return tenantService.list(page, size, search, sort);
  }

  @PostMapping
  public TenantResponse create(@Valid @RequestBody TenantRequest request) {
    return tenantService.create(request);
  }

  @PutMapping("/{id}")
  public TenantResponse update(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
    return tenantService.update(id, request);
  }
}
