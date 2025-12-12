package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

/**
 * Represents a specific entry in a user's collection binder, tracking the
 * quantity of a particular Magic: The Gathering card they own.
 */
@Entity
@Table(name = "binders")
@Data
@NoArgsConstructor
public class Binder {

    /**
     * The unique identifier for this binder entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "binder_id")
    private Long id;

    /**
     * The {@link User} who owns this binder entry.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The specific {@link Card} printing (UUID) being stored.
     */
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card; 

    /**
     * The quantity of this card printing owned by the user. Must be non-negative.
     */
    @Column(nullable = false)
    @Min(value = 0)
    private Integer quantity;
}