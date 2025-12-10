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

    public List<Binder> getUserBinder(Long userId) {
        return binderRepository.findByUserId(userId);
    }

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

    public Page<Binder> searchUserBinder(Long userId, String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return binderRepository.findByUserIdAndCardNameContainingIgnoreCase(userId, "", pageable); 
        }
        return binderRepository.findByUserIdAndCardNameContainingIgnoreCase(userId, name, pageable);
    }

    //Helper for deck Service
    public int getCardQuantity(Long userId, UUID cardId) {
        return binderRepository.findByUserIdAndCardId(userId, cardId)
                .map(Binder::getQuantity)
                .orElse(0);
    }
}