package com.abrams.magic_db.controller;

import com.abrams.magic_db.model.Binder;
import com.abrams.magic_db.service.BinderService;
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

    // GET /api/binder/1
    @GetMapping("/{userId}")
    public List<Binder> getUserBinder(@PathVariable Long userId) {
        return binderService.getUserBinder(userId);
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
}