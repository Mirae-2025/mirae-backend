package com.yourcode.mirae.speedrun.repository;

import com.yourcode.mirae.speedrun.entity.SpeedrunResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeedrunResultRepository extends JpaRepository<SpeedrunResult, Long> {

    List<SpeedrunResult> findBySessionIdOrderByFinalRankAsc(String sessionId);

    List<SpeedrunResult> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<SpeedrunResult> findByUserId(Long userId, Pageable pageable);

    Optional<SpeedrunResult> findBySessionIdAndUserId(String sessionId, Long userId);

    @Query("SELECT AVG(r.finalScore) FROM SpeedrunResult r WHERE r.userId = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(r.solvedCount) FROM SpeedrunResult r WHERE r.userId = :userId")
    Long getTotalSolvedByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM SpeedrunResult r WHERE r.userId = :userId")
    Long getGameCountByUserId(@Param("userId") Long userId);
}
