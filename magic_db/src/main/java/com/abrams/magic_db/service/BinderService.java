package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Binder;
import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.User;
import com.abrams.magic_db.repository.BinderRepository;
import com.abrams.magic_db.repository.CardRepository;
import com.abrams.magic_db.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing a user's card collection (Binder).
 * Handles adding, removing, and querying card quantities.
 */
@Service
public class BinderService {

    private final BinderRepository binderRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public BinderService(BinderRepository binderRepository, CardRepository cardRepository, UserRepository userRepository) {
        this.binderRepository = binderRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the entire list of {@link Binder} entries for a specific user.
     * @param userId The ID of the user.
     * @return A list of all cards and quantities owned by the user.
     */
    public List<Binder> getUserBinder(Long userId) {
        return binderRepository.findByUserId(userId);
    }

    /**
     * Adds a specified quantity of a card to the user's binder.
     * If the card already exists, the quantity is updated. Otherwise, a new entry is created.
     * * @param userId The ID of the user whose binder is being modified.
     * @param cardId The UUID of the card printing to add.
     * @param quantity The amount to add (must be positive).
     * @return The updated or newly created Binder entry.
     * @throws IllegalArgumentException if quantity is not positive.
     * @throws RuntimeException if the User or Card is not found.
     */
    @Transactional
    public Binder addCardToBinder(Long userId, UUID cardId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        Optional<Binder> existingEntry = binderRepository.findByUserIdAndCardId(userId, cardId);

        if (existingEntry.isPresent()) {
            Binder binder = existingEntry.get();
            binder.setQuantity(binder.getQuantity() + quantity);
            return binderRepository.save(binder);
        } else {
            Binder newBinder = new Binder();
            newBinder.setUser(user);
            newBinder.setCard(card);
            newBinder.setQuantity(quantity);
            return binderRepository.save(newBinder);
        }
    }

    /**
     * Removes a specified quantity of a card from the user's binder.
     * If the quantity reaches zero, the binder entry is deleted.
     * This is used for general binder management and deck building (pulling from inventory).
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing to remove.
     * @param quantity The amount to remove (must be positive).
     * @throws RuntimeException if the card is not found or if the removal quantity exceeds owned quantity.
     */
    @Transactional
    public void removeCardFromBinder(Long userId, UUID cardId, int quantity) {
        Binder binder = binderRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new RuntimeException("Card not found in user's binder"));

        int newQuantity = binder.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new RuntimeException("Not enough cards in binder to remove");
        } else if (newQuantity == 0) {
            binderRepository.delete(binder);
        } else {
            binder.setQuantity(newQuantity);
            binderRepository.save(binder);
        }
    }

    /**
     * Searches a user's binder for cards whose name matches the search term.
     * Results are paginated. If the name is null or empty, returns all entries paginated.
     * * @param userId The ID of the user.
     * @param name The search term for the card name (case-insensitive).
     * @param pageable Pagination information (page number and size).
     * @return A {@link Page} of {@link Binder} entries matching the criteria.
     */
    public Page<Binder> searchUserBinder(Long userId, String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return binderRepository.findByUserIdAndCardNameContainingIgnoreCase(userId, "", pageable); 
        }
        return binderRepository.findByUserIdAndCardNameContainingIgnoreCase(userId, name, pageable);
    }

    /**
     * Retrieves the current owned quantity of a specific card printing for a user.
     * This is primarily a helper for the {@link DeckService} to perform inventory checks.
     * * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @return The quantity owned, or 0 if the entry does not exist.
     */
    public int getCardQuantity(Long userId, UUID cardId) {
        return binderRepository.findByUserIdAndCardId(userId, cardId)
                .map(Binder::getQuantity)
                .orElse(0);
    }
}