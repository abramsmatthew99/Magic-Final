import React from 'react';
import '../pages/CardSearch.css'; // Reusing the CSS

const CardLayout = ({ 
    title, 
    searchTerm, 
    onSearchTermChange, 
    onSearchSubmit, 
    page, 
    totalPages, 
    onPageChange, 
    loading, 
    cards, 
    renderCardActions,
    emptyMessage = "No cards found."
}) => {
    return (
        <div className="search-page">
            <div className="search-header">
                {title && <h2 style={{marginRight: '20px'}}>{title}</h2>}
                <form onSubmit={onSearchSubmit} className="search-form">
                    <input 
                        type="text" 
                        className="search-input"
                        placeholder="Search..." 
                        value={searchTerm}
                        onChange={(e) => onSearchTermChange(e.target.value)}
                    />
                    <button className="btn btn-primary" type="submit">Search</button>
                </form>
            </div>

            <div className="pagination-controls">
                <button className="btn btn-secondary" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
                    &laquo; Previous
                </button>
                <span className="page-info">Page {page + 1} of {totalPages || 1}</span>
                <button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>
                    Next &raquo;
                </button>
            </div>

            {loading ? (
                <div className="loading-spinner">Loading...</div>
            ) : (
                <div className="card-grid">
                    {cards.length > 0 ? (
                        cards.map(item => {
                            //here because a split card might have an item.card property. Makes handling binder entries so much simpler oh my 
                            const card = item.card || item; 
                            
                            let imageUrl = "https://via.placeholder.com/250x350?text=No+Image";
                            if (card.imageUrl) {
                                imageUrl = card.imageUrl;
                            } else if (card.faces && card.faces.length > 0 && card.faces[0].imageUrl) {
                                imageUrl = card.faces[0].imageUrl;
                            }

                            return (
                                <div className="magic-card" key={item.binderId || card.id}>
                                    <div className="card-image-wrapper">
                                        <img src={imageUrl} alt={card.name} loading="lazy" />
                                        
                                        {item.quantity !== undefined && item.quantity > 0 && (
                                            <div className="card-quantity-badge">
                                                x{item.quantity}
                                            </div>
                                        )}
                                    </div>
                                    <div className="card-info">
                                        <h3>{card.name}</h3>
                                        <p>{card.rarity} • {card.setCode?.toUpperCase()}</p>
                                        
                                        {renderCardActions && renderCardActions(item)}
                                    </div>
                                </div>
                            );
                        })
                    ) : (
                        <div className="no-results">{emptyMessage}</div>
                    )}
                </div>
            )}
        </div>
    );
};

export default CardLayout;