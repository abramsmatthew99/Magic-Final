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

    @PutMapping("/{deckId}")
    public Deck updateDeck(
            @PathVariable Long deckId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String notes
    ) {
        return deckService.updateDeck(deckId, name, format, notes);
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

    @DeleteMapping("/{deckId}/remove")
    public void removeCardFromDeck(
        @PathVariable Long deckId, 
        @RequestParam UUID cardId, 
        @RequestParam(defaultValue = "1") int quantity
    ) {
        deckService.removeCardFromDeck(deckId, cardId, quantity);
    }

    // Get Single Deck Details (for the Deck Builder page)
    @GetMapping("/{deckId}")
    public Deck getDeckById(@PathVariable Long deckId) {
        return deckService.getDeckById(deckId); 
    }

    //Export Deck List (Text format)
    @GetMapping("/{deckId}/export")
    public String exportDeck(@PathVariable Long deckId) {
        return deckService.exportDeck(deckId); 
    }

    @PutMapping("/transfer")
    public void transferCard(
        @RequestParam Long sourceId,
        @RequestParam Long destId,
        @RequestParam UUID cardId,
        @RequestParam int quantity
    ) {
        deckService.transferCardBetweenDecks(sourceId, destId, cardId, quantity);
    }
}