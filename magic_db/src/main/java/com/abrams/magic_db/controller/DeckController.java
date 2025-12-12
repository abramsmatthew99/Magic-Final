package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Deck;
import com.abrams.magic_db.model.DeckCard;
import com.abrams.magic_db.service.DeckService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing a user's decks and deck contents.
 */
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    /**
     * Retrieves all decks belonging to a specific user.
     * * @param userId The ID of the deck owner.
     * @return A list of {@link Deck} objects, including calculated card counts.
     */
    @GetMapping("/user/{userId}")
    public List<Deck> getUserDecks(@PathVariable Long userId) {
        return deckService.getUserDecks(userId);
    }

    /**
     * Retrieves a single deck by its ID. Used by the Deck Builder page.
     * * @param deckId The ID of the deck.
     * @return The requested {@link Deck} object.
     */
    @GetMapping("/{deckId}")
    public Deck getDeckById(@PathVariable Long deckId) {
        return deckService.getDeckById(deckId); 
    }

    /**
     * Creates a new deck for a specific user.
     * * @param userId The ID of the user creating the deck.
     * @param name The name of the new deck.
     * @param format The format of the new deck.
     * @return The newly created {@link Deck}.
     */
    @PostMapping("/create")
    public Deck createDeck(@RequestParam Long userId, @RequestParam String name, @RequestParam String format) {
        return deckService.createDeck(userId, name, format);
    }

    /**
     * Deletes an entire deck. All associated cards are returned to the user's binder.
     * * @param deckId The ID of the deck to delete.
     */
    @DeleteMapping("/{deckId}")
    public void deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
    }

    /**
     * Updates the metadata (name, format, notes) of an existing deck.
     * * @param deckId The ID of the deck to update.
     * @param name The new name (optional).
     * @param format The new format (optional).
     * @param notes The new notes (optional).
     * @return The updated {@link Deck}.
     */
    @PutMapping("/{deckId}")
    public Deck updateDeck(
            @PathVariable Long deckId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String notes
    ) {
        return deckService.updateDeck(deckId, name, format, notes);
    }

    /**
     * Exports the deck list to a text format.
     * * @param deckId The ID of the deck to export.
     * @return A string containing the formatted deck list.
     */
    @GetMapping("/{deckId}/export")
    public String exportDeck(@PathVariable Long deckId) {
        return deckService.exportDeck(deckId); 
    }

    /**
     * Atomically transfers a quantity of a card from one deck to another.
     * * @param sourceId The ID of the deck to remove the card from.
     * @param destId The ID of the deck to add the card to.
     * @param cardId The UUID of the card printing being moved.
     * @param quantity The amount to transfer.
     */
    @PutMapping("/transfer")
    public void transferCard(
        @RequestParam Long sourceId,
        @RequestParam Long destId,
        @RequestParam UUID cardId,
        @RequestParam int quantity
    ) {
        deckService.transferCardBetweenDecks(sourceId, destId, cardId, quantity);
    }

    /**
     * Adds a specified quantity of a card to a deck. Inventory is checked and consumed from the binder.
     * * @param deckId The ID of the deck to add the card to.
     * @param cardId The UUID of the card printing.
     * @param quantity The quantity to add (default 1).
     * @param isSideboard Flag indicating if the card goes to the sideboard (default false).
     * @return The updated or new {@link DeckCard} entry.
     */
    @PostMapping("/{deckId}/add")
    public DeckCard addCardToDeck(
            @PathVariable Long deckId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(defaultValue = "false") boolean isSideboard
    ) {
        return deckService.addCardToDeck(deckId, cardId, quantity, isSideboard);
    }

    /**
     * Removes a specified quantity of a card from a deck. Inventory is returned to the binder.
     * * @param deckId The ID of the deck to remove the card from.
     * @param cardId The UUID of the card printing.
     * @param quantity The quantity to remove (default 1).
     */
    @DeleteMapping("/{deckId}/remove")
    public void removeCardFromDeck(
        @PathVariable Long deckId, 
        @RequestParam UUID cardId, 
        @RequestParam(defaultValue = "1") int quantity
    ) {
        deckService.removeCardFromDeck(deckId, cardId, quantity);
    }
}