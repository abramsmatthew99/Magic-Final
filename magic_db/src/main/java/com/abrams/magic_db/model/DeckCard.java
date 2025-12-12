package com.abrams.magic_db.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

/**
 * Represents a specific card and quantity included within a {@link Deck}.
 */
@Entity
@Table(name = "deck_cards")
@Data
@NoArgsConstructor
public class DeckCard {

    /**
     * The unique identifier for this entry in the deck list.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The {@link Deck} this entry belongs to. Ignored during JSON serialization.
     */
    @ManyToOne
    @JoinColumn(name = "deck_id", nullable = false)
    @JsonIgnore
    private Deck deck;

    /**
     * The specific {@link Card} printing (UUID) included in the deck.
     */
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /**
     * The quantity of this card included. Must be at least 1.
     */
    @Column(nullable = false)
    @Min(value = 1)
    private Integer quantity;

    /**
     * Flag indicating if the card is in the sideboard (true) or main deck (false).
     */
    @Column(name = "is_sideboard")
    private Boolean isSideboard = false;
}