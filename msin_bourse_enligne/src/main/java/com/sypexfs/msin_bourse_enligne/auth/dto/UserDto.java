package com.sypexfs.msin_bourse_enligne.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String ucode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Boolean enabled;
    private Boolean signatory;
    private UserProfileDto profile;
}
