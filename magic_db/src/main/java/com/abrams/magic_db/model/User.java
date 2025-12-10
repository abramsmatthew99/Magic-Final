package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;

    // A User's collection is just a list of binder entries
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Binder> binderCards;

    // A User can have many decks
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Deck> decks;
}
