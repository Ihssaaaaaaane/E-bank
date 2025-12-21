// src/api/AuthService.js
import axios from 'axios';
import { jwtDecode } from 'jwt-decode'; // CORRECTION ICI

const API_URL = 'http://localhost:8080/auth/'; 

// Fonction de login (UC-1)
export const login = async (username, password) => {
    const response = await axios.post(API_URL + 'login', {
        username,
        password,
    });
    
    if (response.data.token) {
        localStorage.setItem("token", response.data.token);
    }
    return response.data;
};

export const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
};

// Fonction utilitaire pour décoder le rôle du token
export const decodeTokenRole = (token) => {
    try {
        const decoded = jwtDecode(token); // CORRECTION ICI
        
        // Le rôle est stocké dans le claim "roles" sous forme d'une liste d'objets { authority: "ROLE_..." }
        const authorities = decoded.roles; 
        
        if (Array.isArray(authorities) && authorities.length > 0 && authorities[0].authority) {
            return authorities[0].authority; // Retourne "CLIENT" ou "AGENT_GUICHET"
        } 
        
        return null; 

    } catch (error) {
        console.error("Erreur de décodage du token:", error);
        return null;
    }
};

export const forgotPassword = async (email) => {
    const response = await axios.post(API_URL + 'forgot-password', { email });
    return response.data;
};

export const resetPassword = async (token, newPassword) => {
    const response = await axios.post(API_URL + 'reset-password', { token, newPassword });
    return response.data;
};