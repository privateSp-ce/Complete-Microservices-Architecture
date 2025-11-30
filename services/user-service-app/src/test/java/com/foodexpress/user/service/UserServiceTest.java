package com.foodexpress.user.service;

import com.foodexpress.user.dto.AddressDto;
import com.foodexpress.user.dto.UserProfileDto;
import com.foodexpress.user.entity.Address;
import com.foodexpress.user.entity.User;
import com.foodexpress.user.repository.AddressRepository;
import com.foodexpress.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addAddress_ShouldSaveAddress() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        AddressDto dto = new AddressDto();
        dto.setStreet("123 Main St");
        dto.setCity("Metropolis");
        dto.setState("NY");
        dto.setZipCode("10001");
        dto.setType(Address.AddressType.HOME);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));

        // Act
        userService.addAddress(dto);

        // Assert
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void getUserAddresses_ShouldReturnList() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        Address address = new Address();
        address.setId(10L);
        address.setStreet("123 Main St");
        address.setUserId(1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(1L)).thenReturn(Collections.singletonList(address));

        // Act
        List<AddressDto> result = userService.getUserAddresses();

        // Assert
        assertEquals(1, result.size());
        assertEquals("123 Main St", result.get(0).getStreet());
    }

    @Test
    void getUserProfile_ShouldReturnProfile() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAvatarUrl("http://avatar.url");
        user.setPreferences(List.of("VEGAN"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));

        // Act
        UserProfileDto result = userService.getUserProfile();

        // Assert
        assertEquals("John Doe", result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals("http://avatar.url", result.getAvatarUrl());
        assertTrue(result.getPreferences().contains("VEGAN"));
    }

    @Test
    void updateUserProfile_ShouldUpdateFields() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        UserProfileDto dto = new UserProfileDto();
        dto.setAvatarUrl("http://new.url");
        dto.setPreferences(List.of("SPICY"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));

        // Act
        userService.updateUserProfile(dto);

        // Assert
        assertEquals("http://new.url", user.getAvatarUrl());
        assertEquals(List.of("SPICY"), user.getPreferences());
        verify(userRepository, times(1)).save(user);
    }
}
