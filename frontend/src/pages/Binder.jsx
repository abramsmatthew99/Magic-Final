import React, { useEffect, useState } from 'react';
import { addCardToBinder, removeCardFromBinder } from '../services/api';
import CardLayout from '../components/CardLayout';
import useCardSearch from '../hooks/useCardSearch'; 

const qtyButtonStyle = { 
    width: '30px', 
    padding: '0', 
    display: 'flex', 
    alignItems: 'center', 
    justifyContent: 'center',
    fontSize: '0.9rem', 
    borderRadius: '4px',
    cursor: 'pointer'
};

const Binder = () => {
    // Hardcoded User ID 
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
    } = useCardSearch(true, userId, 20); 

    useEffect(() => {
        handleSearchSubmit(); 
    }, []);


    const handleQuantityChange = async (binderEntry, change) => {
        if (!binderEntry || !binderEntry.card || !binderEntry.card.id) {
            console.error("Invalid binder entry:", binderEntry);
            return;
        }

        const cardId = binderEntry.card.id;
        
        try {
            if (change > 0) {
                await addCardToBinder(userId, cardId, change);
            } else {
                await removeCardFromBinder(userId, cardId, Math.abs(change));
            }
            performSearch();
        } catch (error) {
            console.error("Quantity update failed:", error);
            const msg = error.response?.data?.message || error.message;
            alert(`Failed to update quantity: ${msg}`);
        }
    };
    
  

    return (
        <CardLayout
            title="My Binder"
            searchTerm={searchTerm}
            onSearchTermChange={handleSearchTermChange}
            onSearchSubmit={handleSearchSubmit}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
            loading={loading}
            cards={cards}
            emptyMessage="Your binder is empty. Add cards from the Search page!"
            
            renderCardActions={(binderEntry) => (
                <div 
                    className="binder-action-group" 
                    style={{
                        display: 'flex', 
                        justifyContent: 'center', 
                        alignItems: 'center', 
                        gap: '8px', 
                        marginTop: '8px'
                    }}
                >
                    <button 
                        className="btn-danger-action" 
                        style={qtyButtonStyle}
                        onClick={() => handleQuantityChange(binderEntry, -1)}
                        disabled={binderEntry.quantity <= 1} 
                    >
                        -
                    </button>
                    
                    <span 
                        className="binder-quantity-count" 
                        style={{ fontWeight: 'bold', minWidth: '20px', textAlign: 'center', color: 'var(--text-main)' }}
                    >
                        {binderEntry.quantity}
                    </span>

                    <button 
                        className="btn-success-action" 
                        style={qtyButtonStyle}
                        onClick={() => handleQuantityChange(binderEntry, 1)}
                    >
                        +
                    </button>
                </div>
            )}
        />
    );
};

export default Binder;