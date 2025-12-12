package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Represents a user of the Magic: The Gathering collection application.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    
    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * The user's chosen username. Must be unique.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The list of {@link Binder} entries representing the user's card collection.
     * Ignored during JSON serialization.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Binder> binderCards;

    /**
     * The list of {@link Deck}s created by the user.
     * Ignored during JSON serialization.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Deck> decks;
}