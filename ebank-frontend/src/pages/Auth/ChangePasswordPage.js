// src/pages/Shared/ChangePasswordPage.js

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import axios from 'axios';
import { FiLock, FiAlertCircle, FiCheckCircle } from 'react-icons/fi'; // Nouvelle icône pour les messages

// CORRECTION DE L'URL API : L'endpoint est /auth/change-password, pas /api/auth
const API_BASE_URL = 'http://localhost:8080/auth'; 

const ChangePasswordPage = () => {
    const { logoutUser } = useAuth();
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // ... (handleChange reste le même) ...
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        // Validation
        if (formData.newPassword !== formData.confirmPassword) {
            setError("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }

        if (formData.newPassword.length < 8) {
            setError("Le nouveau mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        try {
            setIsLoading(true);
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error("Utilisateur non authentifié.");
            }

            const response = await axios.post(
                `${API_BASE_URL}/change-password`, // URL corrigée
                {
                    currentPassword: formData.currentPassword,
                    newPassword: formData.newPassword
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            setSuccess(response.data || 'Mot de passe modifié avec succès. Déconnexion dans 2s...');
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });

            // Déconnexion après changement pour obliger l'utilisation du nouveau mot de passe
            setTimeout(() => {
                logoutUser();
                navigate('/login');
            }, 2000);

        } catch (err) {
            console.error('Erreur lors du changement de mot de passe:', err);
            
            // Gestion des erreurs du Backend (comme 'Mot de passe actuel est incorrect')
            const backendError = err.response?.data?.message || err.response?.data;
            
            setError(
                backendError || 'Une erreur est survenue lors du changement de mot de passe.'
            );
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-gray-50 p-4">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
                        <FiLock className="text-white w-8 h-8" />
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">Sécurité du Compte</h1>
                    <p className="text-gray-600 mt-2">Mettez à jour votre mot de passe pour garantir la sécurité</p>
                </div>

                <div className="bg-white rounded-xl shadow-xl p-8"> {/* Shadow-xl pour un look plus premium */}
                    
                    {/* Affichage des Erreurs */}
                    {error && (
                        <div className="mb-6 p-4 bg-red-100 border-l-4 border-red-500 text-red-700 flex items-center space-x-2 rounded-md">
                            <FiAlertCircle className="w-5 h-5 flex-shrink-0" />
                            <p className="text-sm font-medium">{error}</p>
                        </div>
                    )}
                    
                    {/* Affichage du Succès */}
                    {success && (
                        <div className="mb-6 p-4 bg-green-100 border-l-4 border-green-500 text-green-700 flex items-center space-x-2 rounded-md">
                            <FiCheckCircle className="w-5 h-5 flex-shrink-0" />
                            <p className="text-sm font-medium">{success}</p>
                        </div>
                    )}

                    <form className="space-y-6" onSubmit={handleSubmit}>
                        {/* Champ Mot de passe actuel */}
                        <div>
                            <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-1">Mot de passe actuel</label>
                            <input
                                id="currentPassword"
                                name="currentPassword"
                                type="password"
                                autoComplete="current-password"
                                required
                                value={formData.currentPassword}
                                onChange={handleChange}
                                className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            />
                        </div>

                        {/* Champ Nouveau mot de passe */}
                        <div>
                            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-1">Nouveau mot de passe</label>
                            <input
                                id="newPassword"
                                name="newPassword"
                                type="password"
                                autoComplete="new-password"
                                required
                                minLength="8"
                                value={formData.newPassword}
                                onChange={handleChange}
                                className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            />
                            <p className="mt-1 text-xs text-gray-500">Le mot de passe doit contenir au moins 8 caractères.</p>
                        </div>

                        {/* Champ Confirmer le nouveau mot de passe */}
                        <div>
                            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">Confirmer le nouveau mot de passe</label>
                            <input
                                id="confirmPassword"
                                name="confirmPassword"
                                type="password"
                                autoComplete="new-password"
                                required
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            />
                        </div>

                        {/* Bouton de soumission */}
                        <div>
                            <button
                                type="submit"
                                disabled={isLoading}
                                className={`w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {isLoading ? 'Modification en cours...' : 'Changer le mot de passe'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ChangePasswordPage;