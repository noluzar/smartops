package com.noluthando.smartops.service;

import com.noluthando.smartops.dto.AuthResponse;
import com.noluthando.smartops.dto.LoginRequest;
import com.noluthando.smartops.dto.RegisterRequest;
import com.noluthando.smartops.entity.User;
import com.noluthando.smartops.enums.Role;
import com.noluthando.smartops.repository.UserRepository;
import com.noluthando.smartops.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Noluthando");
        registerRequest.setEmail("noluthando@gmail.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("noluthando@gmail.com");
        loginRequest.setPassword("password123");

        mockUser = User.builder()
                .id(1L)
                .fullName("Noluthando")
                .email("noluthando@gmail.com")
                .password("encodedPassword")
                .role(Role.MEMBER)
                .build();
    }

    // Register success
    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("noluthando@gmail.com", response.getEmail());
        assertEquals("MEMBER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Register with duplicate email
    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // Register with ADMIN role
    @Test
    void register_ShouldAssignAdminRole_WhenRoleIsAdmin() {
        registerRequest.setRole(Role.ADMIN);

        User adminUser = User.builder()
                .id(2L)
                .fullName("Noluthando")
                .email("noluthando@gmail.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .build();

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("adminToken");

        AuthResponse response = authService.register(registerRequest);

        assertEquals("ADMIN", response.getRole());
    }

    // Register defaults to MEMBER when no role provided
    @Test
    void register_ShouldDefaultToMemberRole_WhenRoleIsNull() {
        registerRequest.setRole(null);

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertEquals("MEMBER", response.getRole());
    }

    // Login success
    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(mockUser)).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("noluthando@gmail.com", response.getEmail());
        assertEquals("MEMBER", response.getRole());
        verify(authenticationManager, times(1)).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
    }

    // Login with wrong credentials
    @Test
    void login_ShouldThrowException_WhenBadCredentials() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));

        verify(userRepository, never()).findByEmail(any());
    }

    // Login with non-existent user
    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
    }
}