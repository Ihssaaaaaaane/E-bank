import { Link, NavLink as RouterNavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  FiLogOut,
  FiUser,
  FiPlusCircle,
  FiHome,
  FiLock
} from 'react-icons/fi';

const Header = () => {
  const { isAuthenticated, userRole, logoutUser, user } = useAuth();
  const navigate = useNavigate();

  if (!isAuthenticated) return null;

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-20 bg-white/80 backdrop-blur-md border-b border-gray-200">
      <nav className="max-w-7xl mx-auto px-6 py-3 flex items-center justify-between">

        {/* ===== LOGO ===== */}
        <Link to="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-blue-500 flex items-center justify-center shadow-md">
            <span className="text-white font-bold text-lg">EB</span>
          </div>
          <div className="leading-tight">
            <span className="block text-lg font-bold text-gray-900">eBank</span>
            <span className="block text-xs text-gray-500">Digital Banking</span>
          </div>
        </Link>

        {/* ===== NAVIGATION ===== */}
        <div className="hidden md:flex items-center gap-2">
          {userRole === 'AGENT_GUICHET' && (
            <>
              <NavItem to="/agent/new-client" icon={<FiUser />}>
                Nouveau client
              </NavItem>
              <NavItem to="/agent/new-account" icon={<FiPlusCircle />}>
                Nouveau compte
              </NavItem>
            </>
          )}

          {userRole === 'CLIENT' && (
            <>
              <NavItem to="/dashboard" icon={<FiHome />}>
                Tableau de bord
              </NavItem>
              <NavItem to="/change-password" icon={<FiLock />}>
                Mot de passe
              </NavItem>
            </>
          )}
        </div>

        {/* ===== USER ACTIONS ===== */}
        <div className="flex items-center gap-4">

          {/* User info */}
          <div className="hidden md:flex items-center gap-3 px-3 py-2 rounded-xl bg-gray-50 border border-gray-200">
            <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center">
              <span className="text-blue-700 font-semibold">
                {user?.username?.charAt(0).toUpperCase()}
              </span>
            </div>

            <div className="leading-tight">
              <p className="text-sm font-semibold text-gray-800">
                {user?.username}
              </p>
              <p className="text-xs text-gray-500">
                {userRole === 'AGENT_GUICHET' ? 'Agent guichet' : 'Client'}
              </p>
            </div>
          </div>

          {/* Logout */}
          <button
            onClick={handleLogout}
            className="p-2.5 rounded-xl text-gray-500 hover:text-red-600 hover:bg-red-50 transition-all"
            title="Déconnexion"
          >
            <FiLogOut className="w-5 h-5" />
          </button>
        </div>

      </nav>
    </header>
  );
};

/* ===== REUSABLE NAV ITEM ===== */
const NavItem = ({ to, icon, children }) => (
  <RouterNavLink
    to={to}
    className={({ isActive }) =>
      `flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all
       ${isActive
         ? 'bg-blue-100 text-blue-700'
         : 'text-gray-600 hover:text-blue-600 hover:bg-blue-50'}`
    }
  >
    <span className="text-lg">{icon}</span>
    {children}
  </RouterNavLink>
);

export default Header;
