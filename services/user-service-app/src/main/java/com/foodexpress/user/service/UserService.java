package com.foodexpress.user.service;

import com.foodexpress.user.dto.AddressDto;
import com.foodexpress.user.dto.UserProfileDto;
import com.foodexpress.user.entity.Address;
import com.foodexpress.user.entity.User;
import com.foodexpress.user.repository.AddressRepository;
import com.foodexpress.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public void addAddress(AddressDto dto) {
        User user = getCurrentUser();
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setType(dto.getType());
        address.setUserId(user.getId());
        addressRepository.save(address);
    }

    public List<AddressDto> getUserAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserProfileDto getUserProfile() {
        User user = getCurrentUser();
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPreferences(user.getPreferences());
        return dto;
    }

    @Transactional
    public void updateUserProfile(UserProfileDto dto) {
        User user = getCurrentUser();
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getPreferences() != null) {
            user.setPreferences(dto.getPreferences());
        }
        userRepository.save(user);
    }

    private AddressDto mapToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setType(address.getType());
        return dto;
    }
}
