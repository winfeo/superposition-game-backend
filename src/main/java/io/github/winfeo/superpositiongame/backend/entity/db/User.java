package io.github.winfeo.superpositiongame.backend.entity.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
@ToString(exclude = {"league", "authorities", "authData", "gamePlayers", "userAchievements"})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "auth_id", unique = true, nullable = false)
    private AuthData authData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "nickname", unique = true, nullable = false, length = 25)
    private String nickname;

    @Column(name = "rating_points", nullable = false)
    private int ratingPoints = 0;

    @Column(name = "wins_amount", nullable = false)
    private int winsAmount = 0;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GamePlayer> gamePlayers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities;

    public void setAuthData(AuthData authData) {
        this.authData = authData;
        authData.setUser(this);
    }

    public String getEmail() { return authData.getEmail(); }

    @Override
    public String getPassword() { return authData.getPasswordHash(); }

    @Override
    public String getUsername() { return authData.getEmail(); }
}
