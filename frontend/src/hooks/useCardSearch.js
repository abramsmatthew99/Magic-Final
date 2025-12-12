import { useState, useEffect } from 'react';
import { searchCards, getUserBinder } from '../services/api';
import { parseQuery } from '../utils/SearchSyntax';

/**
 * Custom hook to manage card search state, pagination, and API calls.
 * Supports both global database search (with advanced syntax) and user binder search.
 *
 * @param {boolean} isBinderSearch If true, searches the user's binder using getUserBinder.
 * @param {Long} userId The ID of the current user (required for binder search).
 * @param {number} pageSize The number of results per page.
 * @param {string} initialSearchTerm The initial search term.
 * @returns {object} Search state and handler functions.
 */
const useCardSearch = (isBinderSearch, userId, pageSize = 20, initialSearchTerm = '') => {
    const [cards, setCards] = useState([]);
    const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        performSearch();
    }, [page]); 

    const performSearch = async () => {
        setLoading(true);
        try {
            let response;
            
            const parsedParams = parseQuery(searchTerm);
            const { name, oracleText, rarity, setCode, cmc, typeLine } = parsedParams;

            if (isBinderSearch) {
                response = await getUserBinder(
                    userId, 
                    name, 
                    oracleText, 
                    rarity, 
                    setCode, 
                    cmc, 
                    typeLine, 
                    page, 
                    pageSize
                );

            } else {
                response = await searchCards(
                    name, 
                    oracleText, 
                    rarity, 
                    setCode, 
                    cmc, 
                    typeLine, 
                    page, 
                    pageSize
                );
            }

            setCards(response.data.content);
            setTotalPages(response.data.totalPages);

        } catch (error) {
            console.error("Error during search:", error);
            setCards([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    };

    const handleSearchSubmit = (e) => {
        if (e) e.preventDefault();
        if (page === 0) performSearch();
        else setPage(0);
    };

    const handleSearchTermChange = (term) => {
        setSearchTerm(term);
    };

    return {
        cards,
        searchTerm,
        loading,
        page,
        totalPages,
        
        setPage,
        handleSearchTermChange,
        handleSearchSubmit,
        performSearch
    };
};

export default useCardSearch;