package com.sypexfs.msin_bourse_enligne.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_profiles", schema = "auth_schema",
        indexes = {
                @Index(name = "idx_profile_ucode", columnList = "ucode")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 50)
    private String ucode;

    @Column(length = 100)
    private String name;

    @Column(name = "admin_role", nullable = false)
    private Boolean adminRole = false;

    @Column(name = "skip_controls", nullable = false)
    private Boolean skipControls = false;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "network_id")
    private Integer networkId;

    @Column(name = "crm_clt_categ", length = 50)
    private String crmCltCateg;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
