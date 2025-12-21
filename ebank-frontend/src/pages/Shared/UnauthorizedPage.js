import { useAuth } from '../../auth/AuthContext';
import { Link } from 'react-router-dom';

const UnauthorizedPage = () => {
    const { logoutUser } = useAuth();

    return (
        <div className="error-container">
            <h1>🚫 Accès Refusé (403)</h1>
            <p>
                Vous n'avez pas le droit d'accéder à cette fonctionnalité. Veuillez contacter votre administrateur.
            </p>
            <p>
                Votre rôle actuel : <strong>{localStorage.getItem('userRole')}</strong>
            </p>
            <button onClick={logoutUser}>
                <Link to="/login" style={{ color: 'white', textDecoration: 'none' }}>Se déconnecter</Link>
            </button>
        </div>
    );
};

export default UnauthorizedPage;