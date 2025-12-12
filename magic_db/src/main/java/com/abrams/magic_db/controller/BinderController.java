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
     * Retrieves a paginated and searchable view of a user's binder, supporting all advanced search criteria.
     * @param userId The ID of the user whose binder to retrieve.
     * @param name Optional search term for card name.
     * @param oracleText Optional search term for rules text.
     * @param rarity Optional filter for rarity.
     * @param setCode Optional filter for set code.
     * @param cmc Optional filter for converted mana cost.
     * @param typeLine Optional filter for type line.
     * @param page The page number (default 0).
     * @param size The number of items per page (default 20).
     * @return A {@link Page} of {@link Binder} entries.
     */
    @GetMapping("/{userId}")
    public Page<Binder> getUserBinder(
            @PathVariable Long userId,
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
        return binderService.searchUserBinder(userId, name, oracleText, rarity, setCode, cmc, typeLine, pageable);
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


    /**
     * Retrieves the quantity of a specific card owned by the user.
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @return The integer quantity of the card owned.
     */
    @GetMapping("/{userId}/card/{cardId}")
    public int getCardQuantity(@PathVariable Long userId, @PathVariable UUID cardId) {
        return binderService.getCardQuantity(userId, cardId); 
    }
}