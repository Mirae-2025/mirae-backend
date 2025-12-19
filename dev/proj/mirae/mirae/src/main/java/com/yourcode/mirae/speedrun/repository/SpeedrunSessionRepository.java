package com.yourcode.mirae.speedrun.repository;

import com.yourcode.mirae.speedrun.entity.SpeedrunSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeedrunSessionRepository extends JpaRepository<SpeedrunSession, Long> {
    Optional<SpeedrunSession> findBySessionId(String sessionId);
}
