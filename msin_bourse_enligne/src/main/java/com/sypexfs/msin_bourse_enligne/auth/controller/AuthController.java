package com.sypexfs.msin_bourse_enligne.auth.controller;

import com.sypexfs.msin_bourse_enligne.auth.dto.*;
import com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl;
import com.sypexfs.msin_bourse_enligne.auth.service.AuthService;
import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful")
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        UserDto userDto = authService.register(request);
        
        return ResponseEntity.ok(
                ApiResponse.success(userDto, "User registered successfully")
        );
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Token refreshed successfully")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String token) {

        authService.logout(token);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Logged out successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserDto userDto = authService.getCurrentUser(userDetails.getId());

        return ResponseEntity.ok(
                ApiResponse.success(userDto, "User retrieved successfully")
        );
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(userDetails.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Password changed successfully")
        );
    }
}