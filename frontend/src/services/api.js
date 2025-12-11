import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
});

// --- CARD API ---
export const searchCards = (name, page = 0, size = 20) => {
    const query = name 
        ? `?name=${encodeURIComponent(name)}&page=${page}&size=${size}` 
        : `?page=${page}&size=${size}`;
    return api.get(`/cards${query}`);
};

export const getCardById = (id) => api.get(`/cards/${id}`);

// --- BINDER API ---
// Note: Hardcoding userId=1 for dev until we add login
export const getUserBinder = (userId, name = '', page = 0, size = 20) => {
    const query = name 
        ? `?name=${encodeURIComponent(name)}&page=${page}&size=${size}` 
        : `?page=${page}&size=${size}`;
    return api.get(`/binder/${userId}${query}`);
};

export const getCardQuantity = (userId, cardId) => api.get(`/binder/${userId}/card/${cardId}`);

export const addCardToBinder = (userId, cardId, quantity = 1) => {
    return api.post(`/binder/${userId}/add?cardId=${cardId}&quantity=${quantity}`);
};

export const removeCardFromBinder = (userId, cardId, quantity = 1) => {
    return api.delete(`/binder/${userId}/remove?cardId=${cardId}&quantity=${quantity}`);
};

// --- DECK API ---
export const getUserDecks = (userId) => api.get(`/decks/user/${userId}`);

export const getDeckById = (deckId) => api.get(`/decks/${deckId}`);

export const createDeck = (userId, name, format) => {
    return api.post(`/decks/create?userId=${userId}&name=${encodeURIComponent(name)}&format=${encodeURIComponent(format)}`);
};

export const updateDeck = (deckId, name, format, notes) => {
    const params = new URLSearchParams();
    if (name) params.append('name', name);
    if (format) params.append('format', format);
    if (notes) params.append('notes', notes);
    return api.put(`/decks/${deckId}?${params.toString()}`);
};

export const deleteDeck = (deckId) => api.delete(`/decks/${deckId}`);

export const exportDeck = (deckId) => api.get(`/decks/${deckId}/export`);

export const addCardToDeck = (deckId, cardId, quantity = 1, isSideboard = false) => {
    return api.post(`/decks/${deckId}/add?cardId=${cardId}&quantity=${quantity}&isSideboard=${isSideboard}`);
};

export const removeCardFromDeck = (deckId, cardId, quantity = 1) => {
    return api.delete(`/decks/${deckId}/remove?cardId=${cardId}&quantity=${quantity}`);
};

export const moveCardToSideboard = (deckId, cardId, toSideboard) => {
    return api.put(`/decks/${deckId}/sideboard?cardId=${cardId}&toSideboard=${toSideboard}`);
};

export const getUserDetails = (userId) => api.get(`/users/${userId}`);

export const transferCardBetweenDecks = (sourceId, destId, cardId, quantity) => 
    api.put(`/decks/transfer?sourceId=${sourceId}&destId=${destId}&cardId=${cardId}&quantity=${quantity}`);

export default api;