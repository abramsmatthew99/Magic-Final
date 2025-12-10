package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.Deck;
import com.abrams.magic_db.model.DeckCard;
import com.abrams.magic_db.model.User;
import com.abrams.magic_db.repository.BinderRepository;
import com.abrams.magic_db.repository.CardRepository;
import com.abrams.magic_db.repository.DeckCardRepository;
import com.abrams.magic_db.repository.DeckRepository;
import com.abrams.magic_db.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<Deck> getUserDecks(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    public Deck createDeck(Long userId, String name, String format) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Deck deck = new Deck();
        deck.setUser(user);
        deck.setName(name);
        deck.setFormat(format);
        return deckRepository.save(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId) {
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        //  Return cards to binder
        for (DeckCard deckCard : deck.getCards()) {
            binderService.addCardToBinder(
                deck.getUser().getId(), 
                deckCard.getCard().getId(), 
                deckCard.getQuantity()
            );
        }
        deckRepository.delete(deck);
    }

    @Transactional
    public DeckCard addCardToDeck(Long deckId, UUID cardId, int quantity, boolean isSideboard) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // --- OWNERSHIP CHECK 
        
        /* //STRICT MODE LOGIC FOR ONLY ALLOWING CARDS IN BINDER TO BE ADDED
        Optional<Binder> binderEntry = binderService.getUserBinder(deck.getUser().getId())
                .stream()
                .filter(b -> b.getCard().getId().equals(cardId))
                .findFirst();
        int ownedQuantity = binderEntry.map(Binder::getQuantity).orElse(0);
        
        Optional<DeckCard> existingCheck = deckCardRepository.findByDeckIdAndCardId(deckId, cardId);
        int inDeckQuantity = existingCheck.map(DeckCard::getQuantity).orElse(0);
        
        if ((inDeckQuantity + quantity) > ownedQuantity) {
             throw new RuntimeException("Not enough cards in binder!");
        }
        */

        //Capacity Check 
        int currentSize = deck.getCards().stream()
                .mapToInt(DeckCard::getQuantity)
                .sum();

        if (deck.getMaxCapacity() != null && (currentSize + quantity) > deck.getMaxCapacity()) {
            throw new RuntimeException("Deck capacity exceeded! Current: " + currentSize 
                                     + ", Max: " + deck.getMaxCapacity() 
                                     + ", Attempted Add: " + quantity);
        }

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
}