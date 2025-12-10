package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Binder;
import com.abrams.magic_db.service.BinderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binder")
public class BinderController {

    private final BinderService binderService;

    public BinderController(BinderService binderService) {
        this.binderService = binderService;
    }

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

    // POST /api/binder/1/add?cardId=...&quantity=1
    @PostMapping("/{userId}/add")
    public Binder addCard(
            @PathVariable Long userId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return binderService.addCardToBinder(userId, cardId, quantity);
    }

    // DELETE /api/binder/1/remove?cardId=...&quantity=1
    @DeleteMapping("/{userId}/remove")
    public String removeCard(
            @PathVariable Long userId,
            @RequestParam UUID cardId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        binderService.removeCardFromBinder(userId, cardId, quantity);
        return "Card removed successfully";
    }


    //  Check quantity of a specific card
    @GetMapping("/{userId}/card/{cardId}")
    public int getCardQuantity(@PathVariable Long userId, @PathVariable UUID cardId) {
        return binderService.getCardQuantity(userId, cardId); 
    }
}