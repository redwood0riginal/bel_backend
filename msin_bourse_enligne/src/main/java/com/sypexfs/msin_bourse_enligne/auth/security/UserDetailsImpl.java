package com.sypexfs.msin_bourse_enligne.auth.security;

import com.sypexfs.msin_bourse_enligne.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        // Get role from profile
        String role = "ROLE_USER";
        if (user.getProfileId() != null) {
            if (Boolean.TRUE.equals(user.getProfileId().getAdminRole())) {
                role = "ROLE_ADMIN";
            }
        }

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getUcode(),
                user.getPasswordHash(),
                user.getEnabled(),
                user.isAccountNonLocked(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
