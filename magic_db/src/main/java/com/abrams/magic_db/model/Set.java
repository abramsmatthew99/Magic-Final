package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Represents a Magic: The Gathering set from which cards are printed.
 */
@Entity
@Table(name = "sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Set {
    
    /**
     * The unique set code (e.g., 'MOM', 'LTR'). This serves as the primary key.
     */
    @Id
    @Column(length =10)
    private String code;

    /**
     * The full name of the set.
     */
    @Column(nullable=false)
    private String name;

    /**
     * The official release date of the set.
     */
    @Column(name = "release_date")
    private LocalDate releaseDate;
}