package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Binder;
import com.abrams.magic_db.service.BinderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing a user's card collection (Binder).
 */
@RestController
@RequestMapping("/api/binder")
public class BinderController {

    private final BinderService binderService;

    public BinderController(BinderService binderService) {
        this.binderService = binderService;
    }

    /**
     * Retrieves a paginated and searchable view of a user's binder.
     * Used by the Binder page and the Deck Builder's autosuggest search.
     * * @param userId The ID of the user whose binder to retrieve.
     * @param name Optional search term for card name.
     * @param page The page number (default 0).
     * @param size The number of items per page (default 20).
     * @return A {@link Page} of {@link Binder} entries.
     */
    @GetMapping("/{userId}")
    public Page<Binder> getUserBinder(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return binderService.searchUserBinder(userId, name, pageable);
    }

    /**
     * Retrieves the quantity of a specific card owned by the user.
     * Used primarily by the Card Search page to display ownership badges.
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @return The integer quantity of the card owned.
     */
    @GetMapping("/{userId}/card/{cardId}")
    public int getCardQuantity(@PathVariable Long userId, @PathVariable UUID cardId) {
        return binderService.getCardQuantity(userId, cardId); 
    }

    /**
     * Adds a specified quantity of a card to the user's binder.
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @param quantity The quantity to add (default 1).
     * @return The updated {@link Binder} entry.
     */
    @PostMapping("/{userId}/add")
    public Binder addCard(
            @PathVariable Long userId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return binderService.addCardToBinder(userId, cardId, quantity);
    }

    /**
     * Removes a specified quantity of a card from the user's binder.
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @param quantity The quantity to remove (default 1).
     * @return Confirmation message upon successful removal/update.
     */
    @DeleteMapping("/{userId}/remove")
    public String removeCard(
            @PathVariable Long userId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        binderService.removeCardFromBinder(userId, cardId, quantity);
        return "Card removed successfully";
    }
}