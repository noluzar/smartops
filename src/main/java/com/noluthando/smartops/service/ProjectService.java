package com.noluthando.smartops.service;

import com.noluthando.smartops.dto.ProjectRequest;
import com.noluthando.smartops.dto.ProjectResponse;
import com.noluthando.smartops.entity.Project;
import com.noluthando.smartops.entity.User;
import com.noluthando.smartops.repository.ProjectRepository;
import com.noluthando.smartops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> getMyProjects() {
        User currentUser = getCurrentUser();
        List<Project> owned = projectRepository.findByOwner(currentUser);
        List<Project> member = projectRepository.findByMembersContaining(currentUser);

        return java.util.stream.Stream.concat(owned.stream(), member.stream())
                .distinct()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return toResponse(project);
    }

    public ProjectResponse addMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        project.getMembers().add(user);
        return toResponse(projectRepository.save(project));
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerName(project.getOwner().getFullName())
                .memberCount(project.getMembers().size())
                .taskCount(project.getTasks().size())
                .createdAt(project.getCreatedAt())
                .build();
    }
}