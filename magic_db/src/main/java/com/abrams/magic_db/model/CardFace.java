package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "card_faces")
@Data
public class CardFace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    @JsonIgnore
    private Card card;

    @Column(name = "face_index", nullable = false)
    private Integer faceIndex; 

    private String name;

    @Column(name = "mana_cost")
    private String manaCost;

    private Double cmc;

    @Column(name = "type_line")
    private String typeLine;
    
    @Column(columnDefinition = "TEXT")
    private String oracleText;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "colors", columnDefinition = "text[]")
    private List<String> colors;
    
    private String power;
    private String toughness;
    private String imageUrl;
}