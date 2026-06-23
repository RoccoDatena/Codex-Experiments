package com.orbit.licensehub.service;

import com.orbit.licensehub.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PageMapperService {

  public <T> PageResponse<T> toResponse(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
