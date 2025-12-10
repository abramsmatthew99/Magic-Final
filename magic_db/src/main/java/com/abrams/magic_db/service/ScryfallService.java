package com.abrams.magic_db.service;

import com.abrams.magic_db.model.Card;
import com.abrams.magic_db.model.CardFace;
import com.abrams.magic_db.model.Set;
import com.abrams.magic_db.repository.CardRepository;
import com.abrams.magic_db.repository.SetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ScryfallService {

    private final CardRepository cardRepository;
    private final SetRepository setRepository;
    private final ObjectMapper objectMapper;

    private static final String BULK_DATA_URL = "https://data.scryfall.io/default-cards/default-cards-20251209102920.json";
    private static final int BATCH_SIZE = 1000; // Save in chunks of 1000 for speed

    public ScryfallService(CardRepository cardRepository, SetRepository setRepository, ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.setRepository = setRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void importCardsFromScryfall() throws IOException {
        System.out.println("Starting Scryfall Import from: " + BULK_DATA_URL);
        
        URL url = new URL(BULK_DATA_URL);
        JsonNode rootNode = objectMapper.readTree(url.openStream());

        // Cache sets to avoid a ton of DB lookups
        java.util.Set<String> knownSets = new HashSet<>();
        setRepository.findAll().forEach(s -> knownSets.add(s.getCode()));

        List<Card> batch = new ArrayList<>();
        int count = 0;

        if (rootNode.isArray()) {
            for (JsonNode cardNode : rootNode) {
                try {
                    // Filter: Only paper games
                    if (cardNode.has("games") && !isPaperGame(cardNode.get("games"))) continue;

                    // 1. Process Set (Check cache first)
                    String setCode = getString(cardNode, "set");
                    if (setCode != null && !knownSets.contains(setCode)) {
                        String setName = getString(cardNode, "set_name");
                        String dateStr = getString(cardNode, "released_at");
                        createSet(setCode, setName, dateStr);
                        knownSets.add(setCode); // Add to cache
                    }

                    // 2. Process Card (Build object, don't save yet)
                    Card card = buildCard(cardNode);
                    if (card != null) {
                        batch.add(card);
                    }

                    // 3. Batch Save
                    if (batch.size() >= BATCH_SIZE) {
                        cardRepository.saveAll(batch);
                        batch.clear(); // Free up memory
                        count += BATCH_SIZE;
                        System.out.println("Saved " + count + " cards...");
                    }

                } catch (Exception e) {
                    // If one card fails, log it and keep going!
                    String cardName = getString(cardNode, "name");
                    System.err.println("Failed to import card: " + (cardName != null ? cardName : "Unknown") + " - " + e.getMessage());
                }
            }
            
            // Save any remaining cards in the final batch
            if (!batch.isEmpty()) {
                cardRepository.saveAll(batch);
                count += batch.size();
            }
        }
        System.out.println("Import Complete! Total cards processed: " + count);
    }

    

    private void createSet(String code, String name, String dateStr) {
        LocalDate date = (dateStr != null) 
            ? LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE) 
            : LocalDate.now(); 
        Set set = new Set(code, name, date);
        setRepository.saveAndFlush(set);
    }

    private Card buildCard(JsonNode node) {
        if (!node.has("id") || !node.has("oracle_id")) return null;

        Card card = new Card();
        card.setId(UUID.fromString(node.get("id").asText()));
        card.setOracleId(UUID.fromString(node.get("oracle_id").asText()));
        card.setName(getString(node, "name"));
        card.setSetCode(getString(node, "set"));
        card.setCollectorNumber(getString(node, "collector_number"));
        card.setRarity(getString(node, "rarity"));
        card.setLayout(getString(node, "layout"));

        List<CardFace> faces = new ArrayList<>();

        if (node.has("card_faces")) {
            int index = 0;
            for (JsonNode faceNode : node.get("card_faces")) {
                CardFace face = mapFace(faceNode);
                face.setFaceIndex(index++);
                face.setCard(card);
                faces.add(face);
            }
        } else {
            CardFace face = mapFace(node);
            face.setFaceIndex(0);
            face.setCard(card);
            faces.add(face);
        }

        card.setFaces(faces);
        return card;
    }

    private CardFace mapFace(JsonNode node) {
        CardFace face = new CardFace();
        face.setName(getString(node, "name"));
        face.setManaCost(getString(node, "mana_cost"));
        face.setCmc(getDouble(node, "cmc"));
        face.setTypeLine(getString(node, "type_line"));
        face.setOracleText(getString(node, "oracle_text"));
        face.setPower(getString(node, "power"));
        face.setToughness(getString(node, "toughness"));
        
        // Array Handling for Colors
        if (node.has("colors") && node.get("colors").isArray()) {
            List<String> colorList = new ArrayList<>();
            for (JsonNode color : node.get("colors")) {
                colorList.add(color.asText());
            }
            face.setColors(colorList);
        }

        if (node.has("image_uris") && node.get("image_uris").has("normal")) {
            face.setImageUrl(node.get("image_uris").get("normal").asText());
        }

        return face;
    }

    private String getString(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }

    private Double getDouble(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asDouble() : null;
    }

    private boolean isPaperGame(JsonNode gamesNode) {
        for (JsonNode game : gamesNode) {
            if (game.asText().equals("paper")) return true;
        }
        return false;
    }
}