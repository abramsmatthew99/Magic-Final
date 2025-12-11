import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import CardSearch from './pages/CardSearch';

// Placeholders for now
const Binder = () => <h2 style={{marginTop: '2rem', textAlign: 'center'}}>My Binder Coming Soon</h2>;
const Decks = () => <h2 style={{marginTop: '2rem', textAlign: 'center'}}>My Decks Coming Soon</h2>;
const Profile = () => <h2 style={{marginTop: '2rem', textAlign: 'center'}}>Profile Coming Soon</h2>;

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
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;