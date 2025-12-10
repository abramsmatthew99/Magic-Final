package com.abrams.magic_db.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Set {
    
    @Id
    @Column(length =10)
    private String code;

    @Column(nullable=false)
    private String name;

    @Column(name = "release_date")
    private LocalDate releaseDate;
}
