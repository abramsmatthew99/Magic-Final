package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    @Query("SELECT d FROM Deck d LEFT JOIN FETCH d.cards WHERE d.user.id = :userId")
    List<Deck> findByUserId(Long userId);
}