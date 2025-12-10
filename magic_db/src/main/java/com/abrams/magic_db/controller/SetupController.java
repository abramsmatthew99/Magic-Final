package com.abrams.magic_db.controller;

import com.abrams.magic_db.service.ScryfallService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/setup")
public class SetupController {

    private final ScryfallService scryfallService;

    public SetupController(ScryfallService scryfallService) {
        this.scryfallService = scryfallService;
    }

    @PostMapping("/import")
    public String runImport() {
        // Run the import in a separate thread so the HTTP request doesn't time out
        // The user will see "Import started!" immediately, while the backend works in the background.
        new Thread(() -> {
            try {
                scryfallService.importCardsFromScryfall();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return "Import started! Check your console logs for progress.";
    }
}