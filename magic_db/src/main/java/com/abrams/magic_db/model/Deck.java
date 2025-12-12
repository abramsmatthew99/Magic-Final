package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "decks")
@Data
@NoArgsConstructor
/**
 * Represents a deck created by a user, containing metadata and a list of {@link DeckCard} entries.
 */
public class Deck {

    /**
     * The unique identifier for the deck.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deck_id")
    private Long id;

    /**
     * The {@link User} who owns this deck.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The name of the deck.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The format the deck is built for (e.g., "Commander", "Standard").
     */
    @Column(nullable = false)
    private String format;

    /**
     * The maximum number of cards allowed in the deck (default 60).
     */
    @Column(name = "max_capacity")
    private Integer maxCapacity = 60;

    /**
     * Notes or strategy text for the deck.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * The list of {@link DeckCard} entries specifying the cards included in the deck.
     */
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckCard> cards;

    /**
     * Transient field used to store the calculated total card quantity
     * for display purposes (not persisted in the database).
     */
    @Transient
    private Integer cardCount;
}