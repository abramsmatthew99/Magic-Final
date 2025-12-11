import React, { useEffect, useState } from 'react';
import { getUserBinder, addCardToBinder, removeCardFromBinder } from '../services/api';
import CardLayout from '../components/CardLayout';

const Binder = () => {
    const [cards, setCards] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    
    // Pagination
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 20;

    // Hardcoded User ID - MUST MATCH YOUR DATABASE (likely 2 based on your previous fix)
    const userId = 2; 

    useEffect(() => {
        performSearch();
    }, [page]); 

    const performSearch = async () => {
        setLoading(true);
        try {
            const response = await getUserBinder(userId, searchTerm, page, pageSize);
            setCards(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error loading binder:", error);
        }
        setLoading(false);
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        if (page === 0) {
            performSearch();
        } else {
            setPage(0);
        }
    };

    const handleQuantityChange = async (binderEntry, change) => {

        if (!binderEntry || !binderEntry.card || !binderEntry.card.id) {
            alert("Error: Invalid card data. Cannot update.");
            return;
        }

        const cardId = binderEntry.card.id;
        
        try {
            if (change > 0) {
                await addCardToBinder(userId, cardId, change);
            } else {
                // When removing, we send the absolute quantity to remove (e.g. 1)
                await removeCardFromBinder(userId, cardId, Math.abs(change));
            }
            // Refresh to get the true state from server
            performSearch();
        } catch (error) {
            console.error("Quantity update failed:", error);
            // Alert the specific error from backend if available
            const msg = error.response?.data?.message || error.message;
            alert(`Failed to update quantity: ${msg}`);
        }
    };

    return (
        <CardLayout
            title="My Binder"
            searchTerm={searchTerm}
            onSearchTermChange={setSearchTerm}
            onSearchSubmit={handleSearchSubmit}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
            loading={loading}
            cards={cards}
            emptyMessage="Your binder is empty (or no cards matched)."
            
            renderCardActions={(binderEntry) => (
                <div className="d-flex justify-content-center align-items-center gap-2 mt-2">
                    <button 
                        className="btn btn-sm btn-outline-danger"
                        style={{ width: '30px', padding: '0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                        onClick={() => handleQuantityChange(binderEntry, -1)}
                    >
                        -
                    </button>
                    
                    <span style={{ fontWeight: 'bold', minWidth: '20px', textAlign: 'center', color: 'var(--text-main)' }}>
                        {binderEntry.quantity}
                    </span>

                    <button 
                        className="btn btn-sm btn-outline-success"
                        style={{ width: '30px', padding: '0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
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