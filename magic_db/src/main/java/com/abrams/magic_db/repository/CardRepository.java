package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.Card;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    
    Page<Card> findByNameContainingIgnoreCase(String name, Pageable pageable);
}