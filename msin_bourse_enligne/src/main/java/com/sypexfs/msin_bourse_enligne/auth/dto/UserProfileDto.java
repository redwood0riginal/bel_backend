package com.sypexfs.msin_bourse_enligne.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Integer id;
    private String ucode;
    private String name;
    private Boolean adminRole;
    private Boolean skipControls;
    private String categoryId;
    private Integer networkId;
    private String crmCltCateg;
}
