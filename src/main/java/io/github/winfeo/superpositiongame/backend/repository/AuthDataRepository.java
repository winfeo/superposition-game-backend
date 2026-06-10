package io.github.winfeo.superpositiongame.backend.repository;

import io.github.winfeo.superpositiongame.backend.entity.db.AuthData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthDataRepository extends JpaRepository<AuthData, Long> {
    Optional<AuthData> findByEmail(String email);
    boolean existsByEmail(String email);
}
