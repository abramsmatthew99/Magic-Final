package com.abrams.magic_db.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cards")
@Data
public class Card {
    
    @Id
    private UUID id; //This refers to the UUID of a unique printing rather than the card object

    @Column(name = "oracle_id")
    private UUID oracleId;

    @Column(nullable = false)
    private String name;

    @Column(name = "set_code")
    private String setCode;

    @Column(name = "collector_number")
    private String collectorNumber;

    private String rarity;
    private String layout;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CardFace> faces;
    
}
