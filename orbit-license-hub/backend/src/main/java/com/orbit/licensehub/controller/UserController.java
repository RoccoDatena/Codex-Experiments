package com.orbit.licensehub.controller;

import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.dto.UserRequest;
import com.orbit.licensehub.dto.UserResponse;
import com.orbit.licensehub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public PageResponse<UserResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "username,asc") String sort,
      @RequestParam(required = false) Long tenantId) {
    return userService.list(page, size, search, sort, tenantId);
  }

  @PostMapping
  public UserResponse create(@Valid @RequestBody UserRequest request) {
    return userService.create(request);
  }
}
