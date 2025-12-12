package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * Represents a single face of a Magic: The Gathering card. For double-faced
 * cards, there will be multiple faces linked to one {@link Card} printing.
 */
@Entity
@Table(name = "card_faces")
@Data
public class CardFace {

    /**
     * The unique identifier for this card face entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long id;

    /**
     * The {@link Card} printing this face belongs to. Ignored during JSON serialization.
     */
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    @JsonIgnore
    private Card card;

    /**
     * The index of the face (0 for front, 1 for back on DFCs).
     */
    @Column(name = "face_index", nullable = false)
    private Integer faceIndex; 

    /**
     * The name of this face.
     */
    private String name;

    /**
     * The mana cost string (e.g., "{2}{U}{U}").
     */
    @Column(name = "mana_cost")
    private String manaCost;

    /**
     * The converted mana cost (CMC) or Mana Value.
     */
    private Double cmc;

    /**
     * The full type line (e.g., "Legendary Creature — Dragon").
     */
    @Column(name = "type_line")
    private String typeLine;
    
    /**
     * The rules text for this face.
     */
    @Column(columnDefinition = "TEXT")
    private String oracleText;

    /**
     * The colors associated with this face. Stored as a PostgreSQL text array.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "colors", columnDefinition = "text[]")
    private List<String> colors;
    
    /**
     * The power of the creature.
     */
    private String power;

    /**
     * The toughness of the creature.
     */
    private String toughness;

    /**
     * URL for the image associated with this card face.
     */
    private String imageUrl;
}