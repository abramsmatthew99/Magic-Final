import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
});

// --- CARD API ---
export const searchCards = (name, oracleText, rarity, setCode, cmc, typeLine, page, size) => {
    let url = `/cards?page=${page}&size=${size}`;
    
    if (name) url += `&name=${encodeURIComponent(name)}`;
    if (oracleText) url += `&oracleText=${encodeURIComponent(oracleText)}`;
    if (rarity) url += `&rarity=${encodeURIComponent(rarity)}`;
    if (setCode) url += `&setCode=${encodeURIComponent(setCode)}`;
    if (cmc != null) url += `&cmc=${cmc}`; 
    if (typeLine) url += `&typeLine=${encodeURIComponent(typeLine)}`;

    return api.get(url);
};
export const getCardSuggestions = (name) => {
    return api.get(`/cards/suggest?name=${encodeURIComponent(name)}`);
};

// --- BINDER API ---
export const getUserBinder = (userId, name, oracleText, rarity, setCode, cmc, typeLine, page, size) => {
    let url = `/binder/${userId}?page=${page}&size=${size}`;
    
    // Append parameters only if they are present (non-null/undefined/empty)
    if (name) url += `&name=${encodeURIComponent(name)}`;
    if (oracleText) url += `&oracleText=${encodeURIComponent(oracleText)}`;
    if (rarity) url += `&rarity=${encodeURIComponent(rarity)}`;
    if (setCode) url += `&setCode=${encodeURIComponent(setCode)}`;
    if (cmc != null) url += `&cmc=${cmc}`; 
    if (typeLine) url += `&typeLine=${encodeURIComponent(typeLine)}`;

    return api.get(url);
};
export const addCardToBinder = (userId, cardId, quantity = 1) => api.post(`/binder/${userId}/add?cardId=${cardId}&quantity=${quantity}`);
export const removeCardFromBinder = (userId, cardId, quantity = 1) => api.delete(`/binder/${userId}/remove?cardId=${cardId}&quantity=${quantity}`);
export const getCardQuantity = (userId, cardId) => api.get(`/binder/${userId}/card/${cardId}`);

// --- DECK API ---
export const getUserDecks = (userId) => api.get(`/decks/user/${userId}`);
export const createDeck = (userId, name, format) => api.post(`/decks/create?userId=${userId}&name=${name}&format=${format}`);
export const deleteDeck = (deckId) => api.delete(`/decks/${deckId}`);
export const getDeckById = (deckId) => api.get(`/decks/${deckId}`);
export const exportDeck = (deckId) => api.get(`/decks/${deckId}/export`);
export const updateDeck = (deckId, name, format, notes) => api.put(`/decks/${deckId}?name=${name}&format=${format}&notes=${notes}`);
export const addCardToDeck = (deckId, cardId, quantity = 1, isSideboard = false) => api.post(`/decks/${deckId}/add?cardId=${cardId}&quantity=${quantity}&isSideboard=${isSideboard}`);
export const removeCardFromDeck = (deckId, cardId, quantity = 1) => api.delete(`/decks/${deckId}/remove?cardId=${cardId}&quantity=${quantity}`);
export const moveCardToSideboard = (deckId, cardId, toSideboard) => api.put(`/decks/${deckId}/sideboard?cardId=${cardId}&toSideboard=${toSideboard}`);
export const transferCardBetweenDecks = (sourceId, destId, cardId, quantity) => 
    api.put(`/decks/transfer?sourceId=${sourceId}&destId=${destId}&cardId=${cardId}&quantity=${quantity}`);

// --- USER API ---
export const getUserDetails = (userId) => api.get(`/users/${userId}`);


export default api;