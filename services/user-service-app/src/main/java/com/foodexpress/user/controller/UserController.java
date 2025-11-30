package com.foodexpress.user.controller;

import com.foodexpress.user.dto.AddressDto;
import com.foodexpress.user.dto.UserProfileDto;
import com.foodexpress.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/addresses")
    public ResponseEntity<Void> addAddress(@Valid @RequestBody AddressDto addressDto) {
        userService.addAddress(addressDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getUserAddresses() {
        return ResponseEntity.ok(userService.getUserAddresses());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody UserProfileDto profileDto) {
        userService.updateUserProfile(profileDto);
        return ResponseEntity.ok().build();
    }
}
