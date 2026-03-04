package com.noluthando.smartops.dto;

import com.noluthando.smartops.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private String projectName;
    private String assigneeName;
    private LocalDateTime createdAt;
}