import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css'; 

const Navbar = () => {
    const location = useLocation();

    return (
        <nav className="navbar">
            <div className="container nav-content">
                <Link className="brand" to="/">Magic Manager</Link>
                
                <ul className="nav-links">
                    <li><Link className={location.pathname === '/search' ? 'active' : ''} to="/search">Search</Link></li>
                    <li><Link className={location.pathname === '/binder' ? 'active' : ''} to="/binder">Binder</Link></li>
                    <li><Link className={location.pathname === '/decks' ? 'active' : ''} to="/decks">Decks</Link></li>
                    <li><Link className={location.pathname === '/profile' ? 'active' : ''} to="/profile">Profile</Link></li>
                </ul>
            </div>
        </nav>
    );
};

export default Navbar;