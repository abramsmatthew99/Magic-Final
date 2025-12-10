package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "deck_cards")
@Data
@NoArgsConstructor
public class DeckCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false)
    @Min(value = 1)
    private Integer quantity;

    @Column(name = "is_sideboard")
    private Boolean isSideboard = false;
}