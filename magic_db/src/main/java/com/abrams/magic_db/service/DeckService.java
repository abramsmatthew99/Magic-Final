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


    public Deck createDeck(Long userId, String name, String format) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Deck deck = new Deck();
        deck.setUser(user);
        deck.setName(name);
        deck.setFormat(format);
        return deckRepository.save(deck);
    }

    //DELETE THE WHOLE DECK 
    @Transactional
    public void deleteDeck(Long deckId) {
        
        Deck deck = getDeckById(deckId);

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

    //ADD CARD TO DECK IN ANY QUANTITY
    @Transactional
    public DeckCard addCardToDeck(Long deckId, UUID cardId, int quantity, boolean isSideboard) {
        Deck deck = getDeckById(deckId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        

        //Capacity Check 
        int currentSize = deck.getCards().stream()
                .mapToInt(DeckCard::getQuantity)
                .sum();

        if (deck.getMaxCapacity() != null && (currentSize + quantity) > deck.getMaxCapacity()) {
            throw new RuntimeException("Deck capacity exceeded!");
        }

        int owned = binderService.getCardQuantity(deck.getUser().getId(), cardId);
        if (owned < quantity) throw new RuntimeException("Not enough cards in your binder to add to the deck");

        //Remove the card from the binder
        binderService.removeCardFromBinder(deck.getUser().getId(), cardId, quantity);

        //Now we can actually add it
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

    //REMOVE UNIQUE CARD FROM DECK IN ANY QUANTITY
    @Transactional
    public void removeCardFromDeck(Long deckid, UUID cardId, int quantity) {
        DeckCard deckCard = deckCardRepository.findByDeckIdAndCardId(deckid, cardId)
            .orElseThrow(() -> new RuntimeException("Card not found in deck!"));
        
            //Quantity Checks
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

    //UPDATE
    public Deck updateDeck(Long deckId, String newName, String newFormat, String newNotes) {
        Deck deck = getDeckById(deckId);
        
        if (newName != null && !newName.isBlank()) deck.setName(newName);
        if (newFormat != null && !newFormat.isBlank()) deck.setFormat(newFormat);
        if (newNotes != null) deck.setNotes(newNotes);

        return deckRepository.save(deck);
    }

    public Deck getDeckById(Long deckId) {
        return deckRepository.findById(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
    }

    //EXPORT FUNCTION maybe unnecessary but idk here it is
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

    @Transactional(readOnly = true)
    public List<Deck> getUserDecks(Long userId) {
        List<Deck> decks = deckRepository.findByUserId(userId);
        
        // Calculate total card count for each deck
        for (Deck deck : decks) {
            int total = deck.getCards().stream()
                             .mapToInt(DeckCard::getQuantity)
                             .sum();
            deck.setCardCount(total);
        }
        
        return decks;
    }

    @Transactional
    public void transferCardBetweenDecks(Long sourceDeckId, Long destDeckId, UUID cardId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transfer quantity must be positive.");
        if (sourceDeckId.equals(destDeckId)) throw new RuntimeException("Source and destination decks cannot be the same.");
        removeCardFromDeck(sourceDeckId, cardId, quantity);
        addCardToDeck(destDeckId, cardId, quantity, false);
    }
}