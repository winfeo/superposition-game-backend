package io.github.winfeo.superpositiongame.backend.repository;

import io.github.winfeo.superpositiongame.backend.entity.db.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("""
            SELECT g FROM Game g
            JOIN GamePlayer gp ON gp.game.id = g.id
            WHERE gp.user.id = :userId
            ORDER BY g.playedAt DESC
            """)
    List<Game> findAllGamesByPlayerId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Game g
            SET g.winner = NULL
            WHERE g.winner.id = :userId
            """)
    void resetWinnerId(@Param("userId") Long userId);
}
