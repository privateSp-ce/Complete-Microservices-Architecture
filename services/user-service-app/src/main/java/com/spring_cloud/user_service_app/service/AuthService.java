package com.spring_cloud.user_service_app.service;

import com.spring_cloud.user_service_app.dto.request.UserLoginRequest;
import com.spring_cloud.user_service_app.dto.request.UserRegistrationRequest;
import com.spring_cloud.user_service_app.dto.response.AuthResponse;
import com.spring_cloud.user_service_app.dto.response.UserResponse;
import com.spring_cloud.user_service_app.entity.User;
import com.spring_cloud.user_service_app.exception.DuplicateResourceException;
import com.spring_cloud.user_service_app.exception.UnauthorizedException;
import com.spring_cloud.user_service_app.repository.UserRepository;
import com.spring_cloud.user_service_app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations (registration, login)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Register a new user
     */
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Check if phone already exists
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone already exists - {}", request.getPhone());
            throw new DuplicateResourceException("User", "phone", request.getPhone());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Get AuthenticationManager
            AuthenticationManager authenticationManager =
                    authenticationConfiguration.getAuthenticationManager();

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            log.debug("Authentication successful for user: {}", request.getEmail());

            // Load user from database
            User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("User not found or inactive"));

            // Generate JWT token
            String token = jwtUtil.generateToken(user);
            log.info("JWT token generated for user: {}", user.getEmail());

            // Build response
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .user(UserResponse.fromEntity(user))
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Login failed: Invalid credentials for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate if email is available for registration
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Validate if phone is available for registration
     */
    public boolean isPhoneAvailable(String phone) {
        return !userRepository.existsByPhone(phone);
    }
}