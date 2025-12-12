import React, { useEffect, useState } from 'react';
import { addCardToBinder, getCardQuantity } from '../services/api';
import GenericModal from '../components/GenericModal';
import CardLayout from '../components/CardLayout';
import { parseQuery, getSyntaxHelp } from '../utils/SearchSyntax'; 
import useCardSearch from '../hooks/useCardSearch'; 
import '../pages/CardSearch.css'; 

const CardSearch = () => {
    // Hardcoded User ID for now
    const userId = 2;

    const {
        cards,
        searchTerm,
        loading,
        page,
        totalPages,
        setPage,
        handleSearchTermChange,
        handleSearchSubmit,
        performSearch 
    } = useCardSearch(false, userId, 20); //

    const [ownedQuantities, setOwnedQuantities] = useState({});
    const [showAddModal, setShowAddModal] = useState(false);
    const [selectedCard, setSelectedCard] = useState(null);
    const [addQuantity, setAddQuantity] = useState(1);
    const [showHelpModal, setShowHelpModal] = useState(false);


    useEffect(() => {
        if (cards.length > 0) {
            fetchOwnershipCounts();
        } else {
            setOwnedQuantities({});
        }
    }, [cards]);

    const fetchOwnershipCounts = async () => {
        const newQuantities = {};
        await Promise.all(cards.map(async (card) => {
            try {
                const response = await getCardQuantity(userId, card.id);
                // Note: Cards retrieved from global search don't have a quantity field initially, 
                // so we rely solely on getCardQuantity
                newQuantities[card.id] = response.data; 
            } catch (err) {
                console.warn(`Failed to fetch quantity for ${card.name}`, err);
            }
        }));
        setOwnedQuantities(newQuantities);
    };

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

    const cardsWithQuantity = cards.map(card => ({
        ...card,
        quantity: ownedQuantities[card.id] || 0
    }));

    return (
        <>
            <div style={{marginTop: '10px', textAlign: 'left', width: '100%'}}>
                <button 
                    className="btn btn-secondary" 
                    onClick={() => setShowHelpModal(true)}
                    style={{padding: '5px 10px', fontSize: '0.85rem', color: 'var(--text-muted)'}}
                >
                    Search Syntax Help
                </button>
            </div>
            <CardLayout 
                title="Card Database"
                searchTerm={searchTerm} 
                onSearchTermChange={handleSearchTermChange} 
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
            >
                

            </CardLayout>

            
            <GenericModal 
                show={showAddModal} 
                onClose={() => setShowAddModal(false)} 
                title={`Add ${selectedCard?.name} to Binder`}
            >
                <form onSubmit={handleAddToBinder}>
                    <div className="form-group">
                        <label>Quantity</label>
                        <input 
                            type="number" 
                            className="form-control" 
                            value={addQuantity} 
                            onChange={(e) => setAddQuantity(e.target.value)} 
                            min="1" 
                            required 
                        />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn btn-secondary me-2" onClick={() => setShowAddModal(false)}>Cancel</button>
                        <button type="submit" className="btn btn-primary">Confirm Add</button>
                    </div>
                </form>
            </GenericModal>

            <GenericModal
                show={showHelpModal}
                onClose={() => setShowHelpModal(false)}
                title="Advanced Search Syntax"
            >
                <p style={{color: 'var(--text-muted)'}}>
                    You can filter by multiple criteria (AND logic) using keyword:value pairs. 
                    Any text not matching a keyword will be searched against the Card Name.
                </p>
                <table className="syntax-table" style={{width: '100%', borderCollapse: 'collapse', marginTop: '15px'}}>
                    <thead>
                        <tr style={{backgroundColor: 'var(--bg-dark)'}}>
                            <th style={{padding: '8px', borderBottom: '1px solid var(--border-color)', textAlign: 'left'}}>Tag</th>
                            <th style={{padding: '8px', borderBottom: '1px solid var(--border-color)', textAlign: 'left'}}>Description / Examples</th>
                        </tr>
                    </thead>
                    <tbody>
                        {getSyntaxHelp().map(({ tag, description }) => (
                            <tr key={tag}>
                                <td style={{padding: '8px', borderBottom: '1px dotted var(--border-color)', fontWeight: 'bold'}}>{tag}</td>
                                <td style={{padding: '8px', borderBottom: '1px dotted var(--border-color)'}}>{description}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <p style={{marginTop: '15px', color: 'var(--text-main)', fontWeight: 'bold'}}>Example:</p>
                <code style={{backgroundColor: '#1c1c1c', padding: '8px', borderRadius: '4px', display: 'block'}}>
                    dragon o:flying cmc:3 r:mythic
                </code>
            </GenericModal>
        </>
    );
};

export default CardSearch;