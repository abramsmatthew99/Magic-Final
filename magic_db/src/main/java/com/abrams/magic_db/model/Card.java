package com.abrams.magic_db.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents a unique printing of a Magic: The Gathering card, holding
 * printing-specific metadata like set code, collector number, and rarity.
 */
@Entity
@Table(name = "cards")
@Data
public class Card {
    
    /**
     * The UUID identifying this unique printing of the card (Scryfall ID). 
     */
    @Id
    private UUID id;

    /**
     * The UUID identifying the card's oracle identity, shared across all printings. 
     * Redundant after database refactoring (one printing per oracleID now)
     */
    @Column(name = "oracle_id")
    private UUID oracleId;

    /**
     * The name of the card.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The set code this card belongs to (e.g., 'ONE', 'DMU').
     */
    @Column(name = "set_code")
    private String setCode;

    /**
     * The collector number for this card within its set.
     */
    @Column(name = "collector_number")
    private String collectorNumber;

    /**
     * The rarity of the card (e.g., 'common', 'rare', 'mythic').
     */
    private String rarity;
    
    /**
     * The physical layout of the card (e.g., 'normal', 'double_faced_token').
     */
    private String layout;

    /**
     * A list of {@link CardFace} objects associated with this card. 
     * This list contains one face for single-faced cards, and multiple for double-faced cards.
     */
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CardFace> faces;
    
}