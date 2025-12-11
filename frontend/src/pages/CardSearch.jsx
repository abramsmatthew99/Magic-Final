import React, { useEffect, useState } from 'react';
import { searchCards, addCardToBinder, getCardQuantity } from '../services/api';
import GenericModal from '../components/GenericModal';
import CardLayout from '../components/CardLayout';

const CardSearch = () => {
    // --- SEARCH STATE ---
    const [cards, setCards] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 20;

    // --- BINDER STATE ---
    const [ownedQuantities, setOwnedQuantities] = useState({});
    
    // Modal State
    const [showAddModal, setShowAddModal] = useState(false);
    const [selectedCard, setSelectedCard] = useState(null);
    const [addQuantity, setAddQuantity] = useState(1);
    
    // Hardcoded User ID for now
    const userId = 2;

    // 1. Fetch Cards on Page Change
    useEffect(() => {
        performSearch();
    }, [page]); 

    // 2. Fetch Ownership when Cards Change
    useEffect(() => {
        if (cards.length > 0) {
            fetchOwnershipCounts();
        }
    }, [cards]);

    const performSearch = async () => {
        setLoading(true);
        try {
            const response = await searchCards(searchTerm, page, pageSize);
            setCards(response.data.content);
            setTotalPages(response.data.totalPages);
            setOwnedQuantities({});
        } catch (error) {
            console.error("Error searching cards:", error);
        }
        setLoading(false);
    };

    const fetchOwnershipCounts = async () => {
        const newQuantities = {};
        await Promise.all(cards.map(async (card) => {
            try {
                const response = await getCardQuantity(userId, card.id);
                newQuantities[card.id] = response.data; 
            } catch (err) {
                console.warn(`Failed to fetch quantity for ${card.name}`, err);
            }
        }));
        setOwnedQuantities(newQuantities);
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        if (page === 0) performSearch();
        else setPage(0);
    };

    // --- ADD TO BINDER HANDLERS ---
    const openAddModal = (card) => {
        setSelectedCard(card);
        setAddQuantity(1);
        setShowAddModal(true);
    };

    const handleAddToBinder = async (e) => {
        e.preventDefault();
        if (!selectedCard) return;

        try {
            await addCardToBinder(userId, selectedCard.id, parseInt(addQuantity));
            alert(`Added ${addQuantity}x ${selectedCard.name} to Binder!`);
            
            setOwnedQuantities(prev => ({
                ...prev,
                [selectedCard.id]: (prev[selectedCard.id] || 0) + parseInt(addQuantity)
            }));
            
            setShowAddModal(false);
        } catch (error) {
            console.error("Error adding to binder:", error);
            alert("Failed to add card.");
        }
    };

    // Prepare cards with quantity for CardLayout
    const cardsWithQuantity = cards.map(card => ({
        ...card,
        quantity: ownedQuantities[card.id] || 0
    }));

    return (
        <>
            <CardLayout 
                title="Card Database"
                searchTerm={searchTerm}
                onSearchTermChange={setSearchTerm}
                onSearchSubmit={handleSearchSubmit}
                page={page}
                totalPages={totalPages}
                onPageChange={setPage}
                loading={loading}
                cards={cardsWithQuantity}
                renderCardActions={(card) => (
                    <button 
                        className="btn btn-add"
                        onClick={() => openAddModal(card)}
                    >
                        + Add to Binder
                    </button>
                )}
            />

            {/* --- ADD TO BINDER MODAL --- */}
            <GenericModal 
                show={showAddModal} 
                onClose={() => setShowAddModal(false)} 
                title={`Add ${selectedCard?.name} to Binder`}
            >
                <form onSubmit={handleAddToBinder}>
                    <div className="mb-3">
                        <label className="form-label">Quantity</label>
                        <input 
                            type="number" 
                            className="form-control" 
                            value={addQuantity} 
                            onChange={(e) => setAddQuantity(e.target.value)} 
                            min="1" 
                            required 
                        />
                    </div>
                    <div className="d-flex justify-content-end gap-2">
                        <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>Cancel</button>
                        <button type="submit" className="btn btn-primary">Confirm Add</button>
                    </div>
                </form>
            </GenericModal>
        </>
    );
};

export default CardSearch;