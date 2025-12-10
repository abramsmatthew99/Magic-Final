package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    //  Get a specific card (for the card details page)
    public Card getCardById(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));
    }

    //searching cards by name (refactor soon)
    public Page<Card> searchCards(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return cardRepository.findAll(pageable);
        }
        return cardRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    //TODO: Add other serach methods
}