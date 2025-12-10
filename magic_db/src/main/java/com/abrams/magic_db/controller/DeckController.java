package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Deck;
import com.abrams.magic_db.model.DeckCard;
import com.abrams.magic_db.service.DeckService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping("/user/{userId}")
    public List<Deck> getUserDecks(@PathVariable Long userId) {
        return deckService.getUserDecks(userId);
    }

    @PostMapping("/create")
    public Deck createDeck(@RequestParam Long userId, @RequestParam String name, @RequestParam String format) {
        return deckService.createDeck(userId, name, format);
    }

    @DeleteMapping("/{deckId}")
    public void deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
    }

    @PostMapping("/{deckId}/add")
    public DeckCard addCardToDeck(
            @PathVariable Long deckId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(defaultValue = "false") boolean isSideboard
    ) {
        return deckService.addCardToDeck(deckId, cardId, quantity, isSideboard);
    }
}