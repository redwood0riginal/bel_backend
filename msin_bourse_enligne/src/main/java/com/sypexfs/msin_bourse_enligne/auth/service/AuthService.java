package com.sypexfs.msin_bourse_enligne.auth.service;

import com.sypexfs.msin_bourse_enligne.auth.dto.*;

public interface AuthService {
    
    LoginResponse login(LoginRequest request);
    
    UserDto register(RegisterRequest request);
    
    LoginResponse refreshToken(RefreshTokenRequest request);
    
    void logout(String token);
    
    UserDto getCurrentUser(Long userId);
    
    void changePassword(Long userId, ChangePasswordRequest request);
}