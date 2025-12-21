import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

const ProtectedRoute = ({ requiredRole }) => {
    const { isAuthenticated, userRole } = useAuth();

    if (!isAuthenticated) {
        // L'application interdit l'accès si l'utilisateur n'est pas authentifié.
        return <Navigate to="/login" replace />; 
    }

    if (requiredRole && userRole !== requiredRole) {
        // "Si l'utilisateur essaye d'accéder à une fonctionnalité dont il n'a pas le droit..."
        return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;