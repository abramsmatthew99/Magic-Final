package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Retrieves a paginated list of all cards, allowing dynamic search filtering by multiple criteria (AND logic).
     * @param name Optional search term for the card name.
     * @param oracleText Optional search term for the card's rules text.
     * @param rarity Optional filter for card rarity.
     * @param setCode Optional filter for the set code.
     * @param cmc Optional filter for converted mana cost (exact match).
     * @param typeLine Optional filter for the card's type line.
     * @param page The page number (default 0).
     * @param size The number of items per page (default 20).
     * @return A {@link Page} of {@link Card} objects matching all provided criteria.
     */
    @GetMapping
    public Page<Card> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String oracleText,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String setCode,
            @RequestParam(required = false) Integer cmc,
            @RequestParam(required = false) String typeLine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return cardService.searchCards(name, oracleText, rarity, setCode, cmc, typeLine, pageable);
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