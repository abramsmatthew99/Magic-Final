import React, { useEffect, useState } from 'react';
import { getUserDecks, createDeck, deleteDeck, exportDeck } from '../services/api';
import GenericTable from '../components/GenericTable';
import GenericModal from '../components/GenericModal';
import { Link } from 'react-router-dom';
import './Decks.css';

const Decks = () => {
    const [decks, setDecks] = useState([]);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newDeckData, setNewDeckData] = useState({ name: '', format: 'Commander' });
    
    // Hardcoded User ID for now
    const userId = 2;

    useEffect(() => {
        loadDecks();
    }, []);

    const loadDecks = async () => {
        try {
            // This API call now returns the 'cardCount' field thanks to the DeckService update
            const response = await getUserDecks(userId);
            setDecks(response.data);
        } catch (error) {
            console.error("Error loading decks:", error);
        }
    };

    const handleCreateDeck = async (e) => {
        e.preventDefault();
        try {
            await createDeck(userId, newDeckData.name, newDeckData.format);
            alert("Deck Created!");
            setShowCreateModal(false);
            setNewDeckData({ name: '', format: 'Commander' });
            loadDecks();
        } catch (error) {
            console.error("Error creating deck:", error);
            alert("Failed to create deck.");
        }
    };

    const handleDelete = async (deckId) => {
        if (window.confirm("Are you sure? This deck will be deleted and cards returned to your binder.")) {
            try {
                await deleteDeck(deckId);
                loadDecks();
            } catch (error) {
                console.error("Error deleting deck:", error);
                alert("Failed to delete deck.");
            }
        }
    };

    const handleExport = async (deckId) => {
        try {
            const response = await exportDeck(deckId);
            const blob = new Blob([response.data], { type: 'text/plain' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `deck-${deckId}.txt`;
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error("Error exporting deck:", error);
            alert("Failed to export deck.");
        }
    };

    const columns = [
        { 
            header: "Name", 
            render: (deck) => (
                <Link to={`/decks/${deck.id}`} className="deck-link">
                    {deck.name}
                </Link>
            ) 
        },
        { header: "Format", key: "format" },
        { 
            header: "Cards", 
            key: "cardCount", 
            render: (deck) => {
                const count = deck.cardCount || 0;
                
                return (
                    <span style={{ fontWeight: 'bold' }}>
                        {count} 
                    </span>
                );
            }
        },
        { header: "Capacity", key: "maxCapacity" }
    ];

    return (
        <div className="decks-container">
            <div className="decks-page-header">
                <h2>My Decks</h2>
                <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
                    + New Deck
                </button>
            </div>

            <GenericTable 
                columns={columns}
                data={decks}
                actions={(deck) => (
                    <div className="deck-actions">
                        <Link to={`/decks/${deck.id}`} className="btn btn-sm btn-outline-primary">
                            Edit
                        </Link>
                        <button className="btn btn-sm btn-outline-secondary" onClick={() => handleExport(deck.id)}>
                            Export
                        </button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(deck.id)}>
                            Delete
                        </button>
                    </div>
                )}
            />

            {/* CREATE DECK MODAL */}
            <GenericModal 
                show={showCreateModal} 
                onClose={() => setShowCreateModal(false)} 
                title="Create New Deck"
            >
                <form onSubmit={handleCreateDeck} className="deck-form">
                    <div className="form-group">
                        <label>Deck Name</label>
                        <input 
                            type="text" 
                            className="form-control" 
                            value={newDeckData.name} 
                            onChange={(e) => setNewDeckData({...newDeckData, name: e.target.value})} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Format</label>
                        <select 
                            className="form-control" 
                            value={newDeckData.format} 
                            onChange={(e) => setNewDeckData({...newDeckData, format: e.target.value})}
                        >
                            <option value="Commander">Commander</option>
                            <option value="Standard">Standard</option>
                            <option value="Modern">Modern</option>
                            <option value="Legacy">Legacy</option>
                        </select>
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancel</button>
                        <button type="submit" className="btn btn-success">Create</button>
                    </div>
                </form>
            </GenericModal>
        </div>
    );
};

export default Decks;