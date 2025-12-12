package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.CardFace;
import com.abrams.magic_db.repository.CardRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Join; 
import jakarta.persistence.criteria.JoinType; 

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    //  Get a specific card (for the card details page)
    public Card getCardById(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));
    }

    /**
     * Searches cards based on multiple, dynamically combined criteria (AND logic).
     * @param nameQuery Optional search term for card name.
     * @param oracleTextQuery Optional search term for oracle text (rules text).
     * @param rarityQuery Optional search term for rarity (e.g., 'rare').
     * @param setCodeQuery Optional search term for set code (e.g., 'neo').
     * @param cmcQuery Optional search term for converted mana cost (exact match for now).
     * @param typeLineQuery Optional search term for card type line (e.g., 'Creature').
     * @return A paginated list of matching cards.
     */
    public Page<Card> searchCards(String nameQuery, String oracleTextQuery, 
                                  String rarityQuery, String setCodeQuery, 
                                  Integer cmcQuery, String typeLineQuery,
                                  Pageable pageable) {
        
        Specification<Card> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            query.distinct(true); 

            // Name
            if (nameQuery != null && !nameQuery.trim().isEmpty()) {
                String likePattern = "%" + nameQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern));
            }

            // Rarity
            if (rarityQuery != null && !rarityQuery.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("rarity")), rarityQuery.toLowerCase()));
            }

            // Set
            if (setCodeQuery != null && !setCodeQuery.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("setCode")), setCodeQuery.toLowerCase()));
            }
            
            // Face Level Searching Moment
            
            Join<Card, CardFace> faceJoin = null;

            // Oracle Text
            if (oracleTextQuery != null && !oracleTextQuery.trim().isEmpty()) {
                if (faceJoin == null) faceJoin = root.join("faces", JoinType.INNER);
                String likePattern = "%" + oracleTextQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(faceJoin.get("oracleText")), likePattern));
            }
            
            // CMC
            if (cmcQuery != null) {
                if (faceJoin == null) faceJoin = root.join("faces", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(faceJoin.get("cmc"), cmcQuery));
            }

            // Type Line
            if (typeLineQuery != null && !typeLineQuery.trim().isEmpty()) {
                if (faceJoin == null) faceJoin = root.join("faces", JoinType.INNER);
                String likePattern = "%" + typeLineQuery.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(faceJoin.get("typeLine")), likePattern));
            }
            

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        

        return cardRepository.findAll(spec, pageable);
    }
};