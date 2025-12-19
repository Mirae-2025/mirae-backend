package com.yourcode.mirae.auth.repository;

import com.yourcode.mirae.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(String uuid);
    Optional<User> findByBaekjunId(String baekjunId);
    boolean existsByEmail(String email);
}