package com.sypexfs.msin_bourse_enligne.auth.service;

import com.sypexfs.msin_bourse_enligne.auth.dto.*;
import com.sypexfs.msin_bourse_enligne.auth.entity.RefreshToken;
import com.sypexfs.msin_bourse_enligne.auth.entity.User;
import com.sypexfs.msin_bourse_enligne.auth.repository.UserRepository;
import com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate user using username (ucode)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate tokens using username (ucode)
        String accessToken = jwtService.generateToken(userDetails.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // Update last login
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        user.setFailedAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);

        // Build response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .user(mapToUserDto(user))
                .build();
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Check if ucode already exists
        if (userRepository.existsByUcode(request.getUcode())) {
            throw new RuntimeException("User code already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUcode(request.getUcode());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setSignatory(false);
        user.setFailedAttempts(0);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        return mapToUserDto(savedUser);
    }

    
    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Generate token using username (ucode)
                    String accessToken = jwtService.generateToken(user.getUcode());
                    return LoginResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(requestRefreshToken)
                            .tokenType("Bearer")
                            .expiresIn(3600L)
                            .user(mapToUserDto(user))
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtService.getUsernameFromToken(token);
        User user = userRepository.findByUcode(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenService.deleteByUserId(user.getId());
    }

    @Override
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDto(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordDate(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate all refresh tokens for security
        refreshTokenService.deleteByUserId(userId);
    }
    
    private UserDto mapToUserDto(User user) {
        UserProfileDto profileDto = null;
        if (user.getProfileId() != null) {
            profileDto = UserProfileDto.builder()
                    .id(user.getProfileId().getId())
                    .ucode(user.getProfileId().getUcode())
                    .name(user.getProfileId().getName())
                    .adminRole(user.getProfileId().getAdminRole())
                    .skipControls(user.getProfileId().getSkipControls())
                    .categoryId(user.getProfileId().getCategoryId())
                    .networkId(user.getProfileId().getNetworkId())
                    .crmCltCateg(user.getProfileId().getCrmCltCateg())
                    .build();
        }

        return UserDto.builder()
                .id(user.getId())
                .ucode(user.getUcode())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.getEnabled())
                .signatory(user.getSignatory())
                .profile(profileDto)
                .build();
    }
}