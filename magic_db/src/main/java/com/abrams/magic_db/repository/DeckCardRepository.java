package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.DeckCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckCardRepository extends JpaRepository<DeckCard, Long> {
    Optional<DeckCard> findByDeckIdAndCardId(Long deckId, UUID cardId);
}