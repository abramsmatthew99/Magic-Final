import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import CardSearch from './pages/CardSearch';
import Binder from './pages/Binder';
import Decks from './pages/Decks';
import DeckBuilder from './pages/DeckBuilder';
import Profile from './pages/Profile';



function App() {
  return (
    <Router>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<Navigate to="/search" />} />
          <Route path="/search" element={<CardSearch />} />
          <Route path="/binder" element={<Binder />} />
          <Route path="/decks" element={<Decks />} />
          <Route path="/decks/:deckId" element={<DeckBuilder />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;