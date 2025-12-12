package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.Deck;
import com.abrams.magic_db.model.DeckCard;
import com.abrams.magic_db.model.User;
import com.abrams.magic_db.repository.CardRepository;
import com.abrams.magic_db.repository.DeckCardRepository;
import com.abrams.magic_db.repository.DeckRepository;
import com.abrams.magic_db.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Service class for managing all business logic related to {@link Deck}s, 
 * including creation, deletion, content management, and atomic card transfers.
 * This service coordinates with the {@link BinderService} for inventory control.
 */
@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final DeckCardRepository deckCardRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final BinderService binderService;

    public DeckService(DeckRepository deckRepository, DeckCardRepository deckCardRepository, 
                       UserRepository userRepository, CardRepository cardRepository, BinderService binderService) {
        this.deckRepository = deckRepository;
        this.deckCardRepository = deckCardRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.binderService = binderService;
    }

    /**
     * Retrieves all decks belonging to a specific user, calculating the current card count 
     * for each deck before returning.
     * * @param userId The ID of the user.
     * @return A list of {@link Deck} objects with the card count populated.
     */
    @Transactional(readOnly = true)
    public List<Deck> getUserDecks(Long userId) {
        List<Deck> decks = deckRepository.findByUserId(userId);
        
        // Calculate total card count for each deck with lots of chained functions, enjoy reading, sorry
        for (Deck deck : decks) {
            int total = deck.getCards().stream()
                             .mapToInt(DeckCard::getQuantity)
                             .sum();
            deck.setCardCount(total);
        }
        
        return decks;
    }
    
    /**
     * Retrieves a single deck by ID.
     * * @param deckId The ID of the deck.
     * @return The requested {@link Deck}.
     * @throws RuntimeException if the deck is not found.
     */
    public Deck getDeckById(Long deckId) {
        return deckRepository.findById(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
    }

    /**
     * Creates a new deck for a user.
     * * @param userId The ID of the user.
     * @param name The name of the new deck.
     * @param format The format of the new deck.
     * @return The newly created {@link Deck}.
     * @throws RuntimeException if the User is not found.
     */
    public Deck createDeck(Long userId, String name, String format) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Deck deck = new Deck();
        deck.setUser(user);
        deck.setName(name);
        deck.setFormat(format);
        return deckRepository.save(deck);
    }

    /**
     * Deletes an entire deck and returns all contained cards to the user's binder.
     * * @param deckId The ID of the deck to delete.
     * @throws RuntimeException if the deck is not found.
     */
    @Transactional
    public void deleteDeck(Long deckId) {
        
        Deck deck = getDeckById(deckId);

        // Return cards to binder
        for (DeckCard deckCard : deck.getCards()) {
            binderService.addCardToBinder(
                deck.getUser().getId(), 
                deckCard.getCard().getId(), 
                deckCard.getQuantity()
            );
        }
        deckRepository.delete(deck);
    }

    /**
     * Adds a card to a deck in a specified quantity. This is a transactional operation
     * that consumes the card quantity from the user's binder.
     * * @param deckId The ID of the deck to modify.
     * @param cardId The UUID of the card printing.
     * @param quantity The amount to add.
     * @param isSideboard Flag indicating placement in the sideboard.
     * @return The updated or newly created {@link DeckCard} entry.
     * @throws RuntimeException if capacity is exceeded, card/deck is not found, or not enough cards in binder.
     */
    @Transactional
    public DeckCard addCardToDeck(Long deckId, UUID cardId, int quantity, boolean isSideboard) {
        Deck deck = getDeckById(deckId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        

        // Capacity Check 
        int currentSize = deck.getCards().stream()
                .mapToInt(DeckCard::getQuantity)
                .sum();

        if (deck.getMaxCapacity() != null && (currentSize + quantity) > deck.getMaxCapacity()) {
            throw new RuntimeException("Deck capacity exceeded!");
        }

        // Inventory Check
        int owned = binderService.getCardQuantity(deck.getUser().getId(), cardId);
        if (owned < quantity) throw new RuntimeException("Not enough cards in your binder to add to the deck");

        // Remove the card from the binder (consumes inventory)
        binderService.removeCardFromBinder(deck.getUser().getId(), cardId, quantity);

        // Now we can actually add it
        Optional<DeckCard> existing = deckCardRepository.findByDeckIdAndCardId(deckId, cardId);

        if (existing.isPresent()) {
            DeckCard deckCard = existing.get();
            deckCard.setQuantity(deckCard.getQuantity() + quantity);
            return deckCardRepository.save(deckCard);
        } else {
            DeckCard deckCard = new DeckCard();
            deckCard.setDeck(deck);
            deckCard.setCard(card);
            deckCard.setQuantity(quantity);
            deckCard.setIsSideboard(isSideboard);
            return deckCardRepository.save(deckCard);
        }
    }

    /**
     * Removes a card from a deck in a specified quantity. Inventory is returned to the binder.
     * * @param deckId The ID of the deck to modify.
     * @param cardId The UUID of the card printing.
     * @param quantity The amount to remove.
     * @throws IllegalArgumentException if quantity is not positive.
     * @throws RuntimeException if the card is not found in the deck or quantity exceeds the deck's count.
     */
    @Transactional
    public void removeCardFromDeck(Long deckId, UUID cardId, int quantity) {
        DeckCard deckCard = deckCardRepository.findByDeckIdAndCardId(deckId, cardId)
            .orElseThrow(() -> new RuntimeException("Card not found in deck!"));
        
            // Quantity Checks
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (deckCard.getQuantity() < quantity) {
            throw new RuntimeException("Cannot remove more cards than exist in the deck.");
        }

         // 1. Return to Binder
        binderService.addCardToBinder(
            deckCard.getDeck().getUser().getId(),
            cardId, 
            quantity
        );

        // 2. Update Deck
        int newQuantity = deckCard.getQuantity() - quantity;
        if (newQuantity == 0) {
            deckCardRepository.delete(deckCard);
        } else {
            deckCard.setQuantity(newQuantity);
            deckCardRepository.save(deckCard);
        }
    }

    /**
     * Updates the metadata (name, format, notes) of an existing deck.
     * * @param deckId The ID of the deck.
     * @param newName The new name.
     * @param newFormat The new format.
     * @param newNotes The new notes.
     * @return The updated {@link Deck}.
     */
    public Deck updateDeck(Long deckId, String newName, String newFormat, String newNotes) {
        Deck deck = getDeckById(deckId);
        
        if (newName != null && !newName.isBlank()) deck.setName(newName);
        if (newFormat != null && !newFormat.isBlank()) deck.setFormat(newFormat);
        if (newNotes != null) deck.setNotes(newNotes);

        return deckRepository.save(deck);
    }

    /**
     * Generates a text representation of the deck list, separating main deck and sideboard.
     * * @param deckId The ID of the deck to export.
     * @return A string containing the formatted deck list.
     */
     public String exportDeck(Long deckId) {
        Deck deck = getDeckById(deckId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("// Deck: ").append(deck.getName()).append("\n");
        sb.append("// Format: ").append(deck.getFormat()).append("\n\n");

        // Main Deck
        deck.getCards().stream()
            .filter(dc -> !dc.getIsSideboard())
            .forEach(dc -> sb.append(dc.getQuantity()).append(" ").append(dc.getCard().getName()).append("\n"));

        // Sideboard
        List<DeckCard> sideboard = deck.getCards().stream()
            .filter(DeckCard::getIsSideboard)
            .collect(Collectors.toList());

        if (!sideboard.isEmpty()) {
            sb.append("\n// Sideboard\n");
            sideboard.forEach(dc -> sb.append(dc.getQuantity()).append(" ").append(dc.getCard().getName()).append("\n"));
        }

        return sb.toString();
    }

    /**
     * Atomically transfers a specified quantity of a card from one deck to another.
     * This relies on the transactional properties of {@link #removeCardFromDeck} (returns to binder)
     * and {@link #addCardToDeck} (consumes from binder, checks capacity).
     * If the destination capacity check fails, the entire transaction rolls back, and the card stays in the source deck.
     * * @param sourceDeckId The ID of the deck to remove the card from.
     * @param destDeckId The ID of the deck to add the card to.
     * @param cardId The UUID of the card printing being moved.
     * @param quantity The amount to transfer.
     * @throws IllegalArgumentException if quantity is not positive.
     * @throws RuntimeException if source and destination are the same, or if any nested operation fails (e.g., capacity).
     */
    @Transactional
    public void transferCardBetweenDecks(Long sourceDeckId, Long destDeckId, UUID cardId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transfer quantity must be positive.");
        if (sourceDeckId.equals(destDeckId)) throw new RuntimeException("Source and destination decks cannot be the same.");

        removeCardFromDeck(sourceDeckId, cardId, quantity);  
        addCardToDeck(destDeckId, cardId, quantity, false);
    }
}