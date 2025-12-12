package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for retrieving card data from the database.
 * This is used for the main card search page.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Retrieves a paginated list of all cards, optionally filtered by name.
     * * @param name Optional search term for the card name (case-insensitive).
     * @param page The page number (default 0).
     * @param size The number of items per page (default 20).
     * @return A {@link Page} of {@link Card} objects.
     */
    @GetMapping
    public Page<Card> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return cardService.searchCards(name, pageable);
    }

    /**
     * Retrieves detailed information for a single card by its UUID.
     * * @param id The UUID of the card printing.
     * @return The {@link Card} object, including its faces.
     */
    @GetMapping("/{id}")
    public Card getCardById(@PathVariable UUID id) {
        return cardService.getCardById(id);
    }
}