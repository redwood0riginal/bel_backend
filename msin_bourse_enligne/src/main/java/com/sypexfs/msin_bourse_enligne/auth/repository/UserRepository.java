package com.sypexfs.msin_bourse_enligne.auth.repository;

import com.sypexfs.msin_bourse_enligne.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUcode(String ucode);

    Boolean existsByEmail(String email);

    Boolean existsByUcode(String ucode);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profileId WHERE u.email = :email")
    Optional<User> findByEmailWithProfile(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profileId WHERE u.ucode = :ucode")
    Optional<User> findByUcodeWithProfile(@Param("ucode") String ucode);
}