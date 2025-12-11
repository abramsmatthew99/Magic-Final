import React, { useEffect, useState } from 'react';
import { searchCards } from '../services/api';
import './CardSearch.css'; 

const CardSearch = () => {
    const [cards, setCards] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    
    // Pagination State
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 20; // Default page size

    // Trigger search on mount and when page changes
    useEffect(() => {
        performSearch();
    }, [page]); 

    const performSearch = async () => {
        setLoading(true);
        try {
            const response = await searchCards(searchTerm, page, pageSize);
            setCards(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error searching cards:", error);
        }
        setLoading(false);
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        setPage(0); // Reset to first page for new searches
        performSearch();
    };

    return (
        <div className="search-page">
            {/* SEARCH BAR SECTION */}
            <div className="search-header">
                <form onSubmit={handleSearchSubmit} className="search-form">
                    <input 
                        type="text" 
                        className="search-input"
                        placeholder="Search for a card (e.g. 'Lotus')..." 
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <button className="btn btn-primary" type="submit">Search</button>
                </form>
            </div>

            {/* PAGINATION TOP */}
            <div className="pagination-controls">
                <button 
                    className="btn btn-secondary" 
                    disabled={page === 0} 
                    onClick={() => setPage(p => p - 1)}
                >
                    &laquo; Previous
                </button>
                <span className="page-info">Page {page + 1} of {totalPages || 1}</span>
                <button 
                    className="btn btn-secondary" 
                    disabled={page >= totalPages - 1} 
                    onClick={() => setPage(p => p + 1)}
                >
                    Next &raquo;
                </button>
            </div>

            {/* RESULTS GRID */}
            {loading ? (
                <div className="loading-spinner">Loading Cards...</div>
            ) : (
                <div className="card-grid">
                    {cards.map(card => {
                        // Logic to find a valid image (Front face or first face)
                        let imageUrl = "https://via.placeholder.com/250x350?text=No+Image";
                        if (card.faces && card.faces.length > 0 && card.faces[0].imageUrl) {
                            imageUrl = card.faces[0].imageUrl;
                        }

                        return (
                            <div className="magic-card" key={card.id}>
                                <div className="card-image-wrapper">
                                    <img src={imageUrl} alt={card.name} loading="lazy" />
                                </div>
                                <div className="card-info">
                                    <h3>{card.name}</h3>
                                    <p>{card.rarity} • {card.setCode?.toUpperCase()}</p>
                                    <button className="btn btn-add">+ Add</button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
            
            {/* PAGINATION BOTTOM (Optional, repeated for UX) */}
            <div className="pagination-controls bottom">
                 {/* (Same buttons as above if desired) */}
            </div>
        </div>
    );
};

export default CardSearch;