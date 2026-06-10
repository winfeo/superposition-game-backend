package io.github.winfeo.superpositiongame.backend.entity.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "league")
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "min_rating", nullable = false)
    private int minRating;

    @Column(name = "max_rating", nullable = false)
    private int maxRating;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "icon_url")
    private String iconUrl;

    @OneToMany(mappedBy = "league", fetch = FetchType.LAZY)
    private List<User> usersInLeague = new ArrayList<>();
}
