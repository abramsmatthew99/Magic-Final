package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "binders")
@Data
@NoArgsConstructor
public class Binder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "binder_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card; 

    @Column(nullable = false)
    @Min(value = 0)
    private Integer quantity;
}