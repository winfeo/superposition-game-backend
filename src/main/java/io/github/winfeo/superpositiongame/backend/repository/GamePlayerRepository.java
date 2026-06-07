package io.github.winfeo.superpositiongame.backend.repository;

import io.github.winfeo.superpositiongame.backend.entity.db.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    @Query("""
            SELECT gp FROM GamePlayer gp
            WHERE gp.game.id = :gameId AND gp.user.id = :userId
            """)
    Optional<GamePlayer> findByGameIdAndPlayerId(
            @Param("gameId") Long gameId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT gp FROM GamePlayer gp
            WHERE gp.game.id = :gameId AND gp.user.id != :userId
            """)
    Optional<GamePlayer> findOpponentByGameIdAndPlayerId(
            @Param("gameId") Long gameId,
            @Param("userId") Long userId
    );

    void deleteByUserId(Long userId);
}
