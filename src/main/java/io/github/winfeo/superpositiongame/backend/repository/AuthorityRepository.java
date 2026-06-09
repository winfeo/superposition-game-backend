package io.github.winfeo.superpositiongame.backend.repository;

import io.github.winfeo.superpositiongame.backend.entity.db.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByAuthority(String authority);
}
