import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { 
    getDeckById, 
    getUserBinder, 
    addCardToDeck, 
    removeCardFromDeck, 
    moveCardToSideboard,
    updateDeck,
    getUserDecks,
    transferCardBetweenDecks 
} from '../services/api';
import GenericModal from '../components/GenericModal';
import './DeckBuilder.css'; 

const useDebounce = (value, delay) => {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);
        return () => clearTimeout(handler);
    }, [value, delay]);
    return debouncedValue;
};

const DeckBuilder = () => {
    const { deckId } = useParams();
    
    // Hardcoded User ID 
    const userId = 2; 

    const [deck, setDeck] = useState(null);
    const [deckCards, setDeckCards] = useState([]);
    const [otherDecks, setOtherDecks] = useState([]); 
    const [loadingDeck, setLoadingDeck] = useState(true);
    const [isEditingMetadata, setIsEditingMetadata] = useState(false);
    const [newDeckName, setNewDeckName] = useState('');
    const [newDeckNotes, setNewDeckNotes] = useState('');

    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState([]); 
    const [loadingSearch, setLoadingSearch] = useState(false);
    const [showResults, setShowResults] = useState(false);
    const debouncedSearchTerm = useDebounce(searchTerm, 300);

    const [showTransferModal, setShowTransferModal] = useState(false);
    const [cardToTransfer, setCardToTransfer] = useState(null); 
    const [transferData, setTransferData] = useState({
        destinationDeckId: '',
        transferQuantity: 1
    });

    useEffect(() => {
        loadDeckDetails();
        loadAllUserDecks(); 
    }, [deckId]);

    const loadDeckDetails = async () => {
        setLoadingDeck(true);
        try {
            const response = await getDeckById(deckId);
            const loadedDeck = response.data;
            setDeck(loadedDeck);
            setDeckCards(loadedDeck.cards || []);
            setNewDeckName(loadedDeck.name);
            setNewDeckNotes(loadedDeck.notes || '');
        } catch (error) {
            console.error("Error loading deck:", error);
            alert("Failed to load deck details.");
        } finally {
            setLoadingDeck(false);
        }
    };
    
    const loadAllUserDecks = async () => {
        try {
            const response = await getUserDecks(userId);
            // Filter out the current deck we are editing
            const filteredDecks = response.data.filter(d => d.id !== parseInt(deckId));
            setOtherDecks(filteredDecks);
        } catch (error) {
            console.error("Error loading other decks:", error);
        }
    };


    useEffect(() => {
        if (debouncedSearchTerm.length > 2) {
            fetchSuggestions(debouncedSearchTerm);
        } else {
            setSearchResults([]);
            setShowResults(false);
        }
    }, [debouncedSearchTerm]);

    const fetchSuggestions = async (query) => {
        setLoadingSearch(true);
        try {
            const response = await getUserBinder(userId, query, 0, 10);
            setSearchResults(response.data.content);
            setShowResults(true);
        } catch (error) {
            console.error("Error fetching suggestions:", error);
            setSearchResults([]);
        } finally {
            setLoadingSearch(false);
        }
    };

    const openTransferModal = (deckCard) => {
        setCardToTransfer(deckCard);
        setTransferData({
            destinationDeckId: otherDecks.length > 0 ? otherDecks[0].id.toString() : '',
            transferQuantity: 1
        });
        setShowTransferModal(true);
    };

    const handleTransferSubmit = async (e) => {
        e.preventDefault();
        
        const sourceDeckId = parseInt(deckId);
        const destDeckId = parseInt(transferData.destinationDeckId);
        const cardId = cardToTransfer.card.id;
        const amount = parseInt(transferData.transferQuantity);
        const availableInSource = cardToTransfer.quantity;

        if (amount <= 0 || amount > availableInSource) {
            alert(`Invalid quantity. Available: ${availableInSource}`);
            return;
        }

        try {
            await transferCardBetweenDecks(sourceDeckId, destDeckId, cardId, amount);
            
            alert(`Successfully moved ${amount}x ${cardToTransfer.card.name} to new deck.`);

            setShowTransferModal(false);
            setCardToTransfer(null);
            loadDeckDetails(); 
        } catch (error) {
            console.error("Transfer failed:", error);
            const msg = error.response?.data?.message || error.message;
            alert(`Transfer Failed: ${msg}. No inventory was lost.`);
        }
    };

    const handleAddCardToDeck = async (binderEntry, isSideboard = false) => {
        if (!deckId) return;

        const card = binderEntry.card;
        const ownedQuantity = binderEntry.quantity;

        if (ownedQuantity < 1) {
            alert("Error: You do not own this card in your binder.");
            return;
        }

        try {
            await addCardToDeck(deckId, card.id, 1, isSideboard);
            loadDeckDetails(); 
            
            setSearchResults(prev => prev.map(item => 
                item.card.id === card.id ? { ...item, quantity: item.quantity - 1 } : item
            ).filter(item => item.quantity > 0)); 
            
            setSearchTerm(''); 
            alert(`Added 1x ${card.name} to ${isSideboard ? 'Sideboard' : 'Main Deck'}!`);
            
        } catch (error) {
            console.error("Failed to add card:", error);
            const msg = error.response?.data?.message || error.message;
            alert(`Failed to add card: ${msg}. Do you own it in your binder?`);
        }
    };
    
    const handleQuantityChange = async (cardId, change) => {
        if (!deckId) return;
        
        try {
            if (change > 0) {
                await addCardToDeck(deckId, cardId, change, false); 
            } else {
                await removeCardFromDeck(deckId, cardId, Math.abs(change));
            }
            loadDeckDetails();
        } catch (error) {
            console.error("Quantity update failed:", error);
            const msg = error.response?.data?.message || error.message;
            alert(`Quantity update failed: ${msg}`);
        }
    };

    const handleMoveToSideboard = async (cardId, toSideboard) => {
        if (!deckId) return;
        try {
            await moveCardToSideboard(deckId, cardId, toSideboard);
            loadDeckDetails();
        } catch (error) {
            console.error("Move failed:", error);
            alert("Failed to move card between main deck and sideboard.");
        }
    };
    
    const handleUpdateMetadata = async () => {
        try {
            await updateDeck(deckId, newDeckName, deck.format, newDeckNotes);
            setIsEditingMetadata(false);
            loadDeckDetails();
        } catch (error) {
            console.error("Metadata update failed:", error);
            alert("Failed to update deck metadata.");
        }
    };
    
    const renderCardRow = (deckCard) => {
        const card = deckCard.card;
        
        return (
            <tr key={deckCard.id}>
                <td>{deckCard.quantity}</td>
                <td>
                    <span className="deck-card-name">{card.name}</span>
                    <span className="text-muted small"> ({card.setCode?.toUpperCase()})</span>
                </td>
                <td className="text-end deck-card-actions">
                    <button 
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => handleQuantityChange(card.id, -1)}
                    >-</button>
                    <button 
                        className="btn btn-sm btn-outline-success ml-2"
                        onClick={() => handleQuantityChange(card.id, 1)}
                    >+</button>
                    <button 
                        className="btn btn-sm btn-outline-secondary ml-2"
                        onClick={() => handleMoveToSideboard(card.id, !deckCard.isSideboard)}
                    >
                        {deckCard.isSideboard ? 'Main' : 'Side'}
                    </button>
                    <button 
                        className="btn btn-sm btn-info ml-2"
                        onClick={() => openTransferModal(deckCard)}
                        title="Transfer to another deck"
                    >
                        Transfer
                    </button>
                </td>
            </tr>
        );
    };
    
    const mainDeck = deckCards.filter(dc => !dc.isSideboard).sort((a, b) => a.card.name.localeCompare(b.card.name));
    const sideboard = deckCards.filter(dc => dc.isSideboard).sort((a, b) => a.card.name.localeCompare(b.card.name));
    const totalCards = mainDeck.reduce((sum, dc) => sum + dc.quantity, 0);
    const totalSideboard = sideboard.reduce((sum, dc) => sum + dc.quantity, 0);


    if (loadingDeck) {
        return <h2 className="loading-message">Loading Deck...</h2>;
    }

    if (!deck) {
        return <h2 className="loading-message">Deck Not Found.</h2>;
    }

    return (
        <div className="deck-builder-page">
            
            <div className="deck-header">
                {isEditingMetadata ? (
                    <div className="metadata-edit-form">
                        <input 
                            type="text" 
                            value={newDeckName} 
                            onChange={(e) => setNewDeckName(e.target.value)} 
                            className="form-control deck-name-input"
                        />
                        <textarea 
                            value={newDeckNotes} 
                            onChange={(e) => setNewDeckNotes(e.target.value)} 
                            className="form-control deck-notes-input"
                            placeholder="Deck Notes (Strategy, history, etc.)"
                        />
                        <div className="metadata-actions">
                            <button className="btn btn-success" onClick={handleUpdateMetadata}>Save</button>
                            <button className="btn btn-secondary" onClick={() => setIsEditingMetadata(false)}>Cancel</button>
                        </div>
                    </div>
                ) : (
                    <>
                        <div className="metadata-display">
                            <h1>{deck.name}</h1>
                            <p>Format: <strong>{deck.format}</strong> | Total Cards: <strong>{totalCards}</strong> ({totalSideboard} in Sideboard)</p>
                            <p className="deck-notes">{deck.notes || "No notes yet."}</p>
                        </div>
                        <button className="btn btn-sm btn-outline-primary" onClick={() => setIsEditingMetadata(true)}>
                            Edit Details
                        </button>
                    </>
                )}
            </div>
            
            <div className="add-card-section">
                <div className="search-bar-wrapper">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search for owned cards to add..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onBlur={() => setTimeout(() => setShowResults(false), 200)}
                        onFocus={() => { if (searchResults.length > 0) setShowResults(true); }}
                    />
                    
                    {showResults && searchTerm.length > 2 && (
                        <div className="search-results-dropdown">
                            {loadingSearch ? (
                                <div className="dropdown-item loading">Searching Binder...</div>
                            ) : searchResults.length > 0 ? (
                                searchResults.map(binderEntry => (
                                    <div key={binderEntry.card.id} className="dropdown-item">
                                        <div className="card-suggestion-name">
                                            {binderEntry.card.name} 
                                            <span className="text-muted small"> (Owned: {binderEntry.quantity})</span>
                                        </div>
                                        <div className="card-suggestion-actions">
                                            <button 
                                                className="btn btn-sm btn-success" 
                                                onClick={() => handleAddCardToDeck(binderEntry, false)}
                                                disabled={binderEntry.quantity < 1}
                                            >+ Main</button>
                                            <button 
                                                className="btn btn-sm btn-secondary"
                                                onClick={() => handleAddCardToDeck(binderEntry, true)}
                                                disabled={binderEntry.quantity < 1}
                                            >+ Side</button>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="dropdown-item no-results">No owned cards found matching "{searchTerm}".</div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            <div className="deck-list-container">
                <h3 className="list-title">Main Deck ({totalCards} Cards)</h3>
                <div className="deck-cards-table-wrapper">
                    <table className="deck-cards-table">
                        <tbody>
                            {mainDeck.map(renderCardRow)}
                        </tbody>
                    </table>
                </div>

                {sideboard.length > 0 && (
                    <>
                        <h3 className="list-title sideboard-title">Sideboard ({totalSideboard} Cards)</h3>
                        <div className="deck-cards-table-wrapper">
                             <table className="deck-cards-table">
                                <tbody>
                                    {sideboard.map(renderCardRow)}
                                </tbody>
                            </table>
                        </div>
                    </>
                )}
                
                {deckCards.length === 0 && (
                    <div className="no-cards-message">Start adding cards using the search bar above!</div>
                )}
            </div>

            <GenericModal
                show={showTransferModal}
                onClose={() => setShowTransferModal(false)}
                title={`Transfer ${cardToTransfer?.card?.name || ''}`}
            >
                <form onSubmit={handleTransferSubmit}>
                    <p className="text-muted">Available in this deck: <strong>{cardToTransfer?.quantity || 0}</strong></p>
                    
                    <div className="form-group">
                        <label>Destination Deck</label>
                        <select 
                            className="form-control" 
                            name="destinationDeckId" 
                            value={transferData.destinationDeckId}
                            onChange={(e) => setTransferData({...transferData, destinationDeckId: e.target.value})}
                            required
                        >
                            <option value="">Select a Deck...</option>
                            {otherDecks.map(deck => (
                                <option key={deck.id} value={deck.id}>
                                    {deck.name} (Format: {deck.format})
                                </option>
                            ))}
                        </select>
                        {otherDecks.length === 0 && (
                            <p className="text-muted small mt-2">Create another deck first!</p>
                        )}
                    </div>

                    <div className="form-group">
                        <label>Quantity to Move</label>
                        <input 
                            type="number" 
                            className="form-control" 
                            name="transferQuantity" 
                            value={transferData.transferQuantity} 
                            onChange={(e) => setTransferData({...transferData, transferQuantity: e.target.value})}
                            min="1" 
                            max={cardToTransfer?.quantity || 1}
                            required
                        />
                    </div>

                    <div className="modal-actions">
                        <button type="button" className="btn btn-secondary" onClick={() => setShowTransferModal(false)}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={!transferData.destinationDeckId || otherDecks.length === 0}>
                            Transfer Card
                        </button>
                    </div>
                </form>
            </GenericModal>
        </div>
    );
};

export default DeckBuilder;