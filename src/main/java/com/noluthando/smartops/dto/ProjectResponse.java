package com.noluthando.smartops.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private int memberCount;
    private int taskCount;
    private LocalDateTime createdAt;
}