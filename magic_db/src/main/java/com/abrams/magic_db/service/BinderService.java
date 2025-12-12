package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Binder;
import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.CardFace;
import com.abrams.magic_db.model.User;
import com.abrams.magic_db.repository.BinderRepository;
import com.abrams.magic_db.repository.CardRepository;
import com.abrams.magic_db.repository.UserRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing a user's card collection (Binder).
 * Handles adding, removing, and querying card quantities, now using Specifications for comprehensive searching.
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
     * @param userId The ID of the user whose binder is being modified.
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
     * @param userId The ID of the user.
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
     * Searches a user's binder using specifications, allowing dynamic filtering
     * based on all major card attributes (Name, Oracle Text, CMC, Rarity, Set Code, Type Line).
     * @param userId The ID of the user whose binder to search.
     * @param nameQuery Optional search term for the card's name.
     * @param oracleTextQuery Optional search term for oracle text (rules text).
     * @param rarityQuery Optional filter for card rarity.
     * @param setCodeQuery Optional filter for set code.
     * @param cmcQuery Optional filter for converted mana cost.
     * @param typeLineQuery Optional filter for card type line.
     * @param pageable Pagination information.
     * @return A paginated list of matching Binder entries.
     */
    public Page<Binder> searchUserBinder(Long userId, String nameQuery, String oracleTextQuery, 
                                          String rarityQuery, String setCodeQuery, 
                                          Integer cmcQuery, String typeLineQuery, 
                                          Pageable pageable) {

        Specification<Binder> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
             //Filter by User ID
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            // Avoid duplicate Binder results when joining CardFaces
            query.distinct(true);

            // Join from Binder to Card 
            Join<Binder, Card> cardJoin = root.join("card", JoinType.INNER);
            
            // Card Name Search
            if (nameQuery != null && !nameQuery.trim().isEmpty()) {
                String likePattern = "%" + nameQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(cardJoin.get("name")), likePattern));
            }
            
            // Rarity Search
            if (rarityQuery != null && !rarityQuery.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(cardJoin.get("rarity")), rarityQuery.toLowerCase()));
            }

            // Set Code Search
            if (setCodeQuery != null && !setCodeQuery.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(cardJoin.get("setCode")), setCodeQuery.toLowerCase()));
            }

            //  Face-Level Filters 
            
            Join<Card, CardFace> faceJoin = null;

            // Oracle Text Search
            if (oracleTextQuery != null && !oracleTextQuery.trim().isEmpty()) {
                if (faceJoin == null) faceJoin = cardJoin.join("faces", JoinType.INNER);
                String likePattern = "%" + oracleTextQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(faceJoin.get("oracleText")), likePattern));
            }

            // CMC Search (exact match)
            if (cmcQuery != null) {
                if (faceJoin == null) faceJoin = cardJoin.join("faces", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(faceJoin.get("cmc"), cmcQuery));
            }

            // Type Line Search (partial match)
            if (typeLineQuery != null && !typeLineQuery.trim().isEmpty()) {
                if (faceJoin == null) faceJoin = cardJoin.join("faces", JoinType.INNER);
                String likePattern = "%" + typeLineQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(faceJoin.get("typeLine")), likePattern));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return binderRepository.findAll(spec, pageable);
    }
    
    /**
     * Retrieves the current owned quantity of a specific card printing for a user.
     * @param userId The ID of the user.
     * @param cardId The UUID of the card printing.
     * @return The quantity owned, or 0 if the entry does not exist.
     */
    public int getCardQuantity(Long userId, UUID cardId) {
        return binderRepository.findByUserIdAndCardId(userId, cardId)
                .map(Binder::getQuantity)
                .orElse(0);
    }
}