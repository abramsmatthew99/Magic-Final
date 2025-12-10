package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    //Service defaults to all cards for null name
    //so this works for finding All or searching by name
    //returns only a page of cards because there's a 
    //whole lotta cards
    @GetMapping
    public Page<Card> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return cardService.searchCards(name, pageable);
    }

    //The card details request basically
    @GetMapping("/{id}")
    public Card getCardById(@PathVariable UUID id) {
        return cardService.getCardById(id);
    }
}
