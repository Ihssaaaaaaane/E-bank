import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import LoginPage from './pages/Auth/LoginPage'; 
import DashboardClient from './pages/Client/DashboardClient'; 
import ChangePasswordPage from './pages/Auth/ChangePasswordPage';
import ForgotPasswordPage from './pages/Auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/Auth/ResetPasswordPage';
import NewClientPage from './pages/Agent/NewClientPage';
import NewAccountPage from './pages/Agent/NewAccountPage'; 
import UnauthorizedPage from './pages/Shared/UnauthorizedPage';
import Header from './components/Header'; 


function App() {
    return (
        <Router>
            <AuthProvider>
                <div className="App">
                    {/* Le Header s'affiche sur toutes les routes, mais sa logique interne 
                        le rend invisible si non authentifié. */}
                    <Header /> 
                    <main style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}> 
                        <Routes>
                            {/* Route Publique */}
                            <Route path="/login" element={<LoginPage />} />
                            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                            <Route path="/reset-password" element={<ResetPasswordPage />} />
                            <Route path="/unauthorized" element={<UnauthorizedPage />} />

                            {/* Redirection vers le dashboard après login */}
                            <Route path="/" element={<Navigate to="/dashboard" replace />} />

                            {/* Routes Protégées CLIENT (UC-4, UC-5) */}
                            <Route element={<ProtectedRoute requiredRole="CLIENT" />}>
                                <Route path="/dashboard" element={<DashboardClient />} />
                                <Route path="/change-password" element={<ChangePasswordPage />} />
                            </Route>

                            {/* Routes Protégées AGENT_GUICHET (UC-2, UC-3) */}
                            <Route element={<ProtectedRoute requiredRole="AGENT_GUICHET" />}>
                                <Route path="/agent/new-client" element={<NewClientPage />} />
                                <Route path="/agent/new-account" element={<NewAccountPage />} />
                            </Route>

                            {/* Fallback 404 */}
                            <Route path="*" element={<h1>404 - Page Non Trouvée</h1>} />
                        </Routes>
                    </main>
                </div>
            </AuthProvider>
        </Router>
    );
}

export default App;