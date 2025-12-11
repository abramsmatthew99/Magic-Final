import React, { useState, useEffect } from 'react';
import { getUserDetails } from '../services/api';
import './Profile.css'; 

const Profile = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Hardcoded User ID 
    const userId = 2;

    useEffect(() => {
        loadProfile();
    }, []);

    const loadProfile = async () => {
        try {
            const response = await getUserDetails(userId);
            setUser(response.data);
            setLoading(false);
        } catch (err) {
            console.error("Error loading profile:", err);
            setError("Failed to load user profile. Check backend logs and ensure User ID 2 exists.");
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="profile-container loading-message">Loading Profile...</div>;
    }

    if (error) {
        return <div className="profile-container error-message">{error}</div>;
    }

    if (!user) {
        return <div className="profile-container error-message">User data is unavailable.</div>;
    }

    return (
        <div className="profile-container">
            <div className="profile-card">
                <h1>{user.username}'s Profile</h1>
                <div className="profile-detail">
                    <span className="label">User ID:</span>
                    <span className="value">{user.id}</span>
                </div>
                <div className="profile-detail">
                    <span className="label">Username:</span>
                    <span className="value">{user.username}</span>
                </div>
                <div className="profile-detail">
                    <span className="label">Email:</span>
                    <span className="value">{user.email || "N/A"}</span>
                </div>
                
                <div className="profile-summary">
                    <p>Current application environment status:</p>
                    <ul>
                        <li><i className="status-icon success">✓</i> All data tied to User ID: <strong>{user.id}</strong></li>
                        <li><i className="status-icon success">✓</i> Binder management active</li>
                        <li><i className="status-icon success">✓</i> Deck building active</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default Profile;