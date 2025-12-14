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
@Table(name = "users", schema = "auth_schema",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_ucode", columnList = "ucode"),
                @Index(name = "idx_users_profile", columnList = "profile_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String ucode;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean signatory = false;

    @Column(name = "employee_number", length = 50)
    private String employeeNumber;

    @Column(name = "function", length = 100)
    private String function;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id")
    private UserProfile profileId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "password_date")
    private LocalDateTime passwordDate;

    @Column(name = "grace_date")
    private LocalDateTime graceDate;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public boolean isAccountNonLocked() {
        return lockTime == null || LocalDateTime.now().isAfter(lockTime.plusHours(24));
    }
}
