package com.sypexfs.msin_bourse_enligne.auth.repository;

import com.sypexfs.msin_bourse_enligne.auth.entity.RefreshToken;
import com.sypexfs.msin_bourse_enligne.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);

    void deleteByToken(String token);
}
