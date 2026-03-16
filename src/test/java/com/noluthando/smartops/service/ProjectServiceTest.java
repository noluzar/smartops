package com.noluthando.smartops.service;

import com.noluthando.smartops.dto.ProjectRequest;
import com.noluthando.smartops.dto.ProjectResponse;
import com.noluthando.smartops.entity.Project;
import com.noluthando.smartops.entity.User;
import com.noluthando.smartops.enums.Role;
import com.noluthando.smartops.repository.ProjectRepository;
import com.noluthando.smartops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private User mockUser;
    private Project mockProject;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .fullName("Noluthando")
                .email("noluthando@gmail.com")
                .password("encodedPassword")
                .role(Role.MEMBER)
                .build();

        mockProject = Project.builder()
                .id(1L)
                .name("SmartOps Platform")
                .description("Main product project")
                .owner(mockUser)
                .members(new ArrayList<>())
                .tasks(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        projectRequest = new ProjectRequest();
        projectRequest.setName("SmartOps Platform");
        projectRequest.setDescription("Main product project");

        // Mock SecurityContextHolder for getCurrentUser()
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("noluthando@gmail.com");
        when(userRepository.findByEmail("noluthando@gmail.com")).thenReturn(Optional.of(mockUser));
    }

    // Create project success
    @Test
    void createProject_ShouldReturnProjectResponse_WhenValidRequest() {
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        ProjectResponse response = projectService.createProject(projectRequest);

        assertNotNull(response);
        assertEquals("SmartOps Platform", response.getName());
        assertEquals("Main product project", response.getDescription());
        assertEquals("Noluthando", response.getOwnerName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    // Get my projects
    @Test
    void getMyProjects_ShouldReturnCombinedList_WhenUserHasProjects() {
        when(projectRepository.findByOwner(mockUser)).thenReturn(List.of(mockProject));
        when(projectRepository.findByMembersContaining(mockUser)).thenReturn(new ArrayList<>());

        List<ProjectResponse> responses = projectService.getMyProjects();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("SmartOps Platform", responses.get(0).getName());
    }

    // Get my projects returns empty list
    @Test
    void getMyProjects_ShouldReturnEmptyList_WhenUserHasNoProjects() {
        when(projectRepository.findByOwner(mockUser)).thenReturn(new ArrayList<>());
        when(projectRepository.findByMembersContaining(mockUser)).thenReturn(new ArrayList<>());

        List<ProjectResponse> responses = projectService.getMyProjects();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // Get project by ID
    @Test
    void getProject_ShouldReturnProject_WhenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));

        ProjectResponse response = projectService.getProject(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SmartOps Platform", response.getName());
    }

    // Get project by ID not found
    @Test
    void getProject_ShouldThrowException_WhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> projectService.getProject(99L));

        assertEquals("Project not found", exception.getMessage());
    }

    // Add member to project
    @Test
    void addMember_ShouldAddUserToProject_WhenValidIds() {
        User newMember = User.builder()
                .id(2L)
                .fullName("New Member")
                .email("member@smartops.com")
                .role(Role.MEMBER)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        ProjectResponse response = projectService.addMember(1L, 2L);

        assertNotNull(response);
        verify(projectRepository, times(1)).save(mockProject);
    }

    // Add member — project not found
    @Test
    void addMember_ShouldThrowException_WhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> projectService.addMember(99L, 1L));
    }

    // Add member — user not found
    @Test
    void addMember_ShouldThrowException_WhenUserNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> projectService.addMember(1L, 99L));
    }

    // Delete project
    @Test
    void deleteProject_ShouldCallRepository_WhenProjectExists() {
        doNothing().when(projectRepository).deleteById(1L);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).deleteById(1L);
    }
}