import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../auth/AuthContext';
import { useNavigate, Navigate } from 'react-router-dom';

const API_URL = 'http://localhost:8080/api/agents/accounts';

const NewAccountPage = () => {
  const { userRole } = useAuth();
  const navigate = useNavigate();

  const [accountData, setAccountData] = useState({
    identityNumber: '',
    rib: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setAccountData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    // Vérification des champs obligatoires
    const requiredFields = ['identityNumber', 'rib'];
    const missing = requiredFields.filter(field => !accountData[field]);
    if (missing.length > 0) {
      setError(`Les champs suivants sont obligatoires : ${missing.join(', ')}`);
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await axios.post(API_URL, accountData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setMessage(response.data);
      setAccountData({ identityNumber: '', rib: '' });

    } catch (err) {
      console.error(err);
      if (err.response && (err.response.status === 409 || err.response.status === 400)) {
        setError("Échec : " + err.response.data);
      } else if (err.response && err.response.status === 403) {
        setError("Accès interdit. Veuillez contacter l’administrateur.");
      } else {
        setError("Erreur serveur. Vérifiez les données ou la connexion.");
      }
    }
  };

  if (userRole !== 'AGENT_GUICHET') {
    return <Navigate to="/unauthorized" replace />;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4">
      <div className="w-full max-w-2xl bg-white rounded-2xl shadow-lg p-8">

        {/* ===== TITLE ===== */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">
            Création d’un nouveau compte bancaire
          </h1>
          <p className="text-sm text-gray-500">
            Interface Agent Guichet
          </p>
        </div>

        {/* ===== ALERTS ===== */}
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-100 text-red-700 text-sm">
            {error}
          </div>
        )}

        {message && (
          <div className="mb-4 p-3 rounded-lg bg-green-100 text-green-700 text-sm">
            {message}
          </div>
        )}

        {/* ===== FORM ===== */}
        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input label="Numéro d'identité du Client " name="identityNumber" value={accountData.identityNumber} onChange={handleChange} />
          <Input label="RIB du Nouveau Compte " name="rib" value={accountData.rib} onChange={handleChange} />

          {/* ===== SUBMIT ===== */}
          <div className="md:col-span-2 mt-4">
            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-3 rounded-xl font-semibold hover:bg-blue-700 transition"
            >
              Créer le compte
            </button>
          </div>
        </form>



      </div>
    </div>
  );
};

/* ===== REUSABLE INPUT COMPONENT ===== */
const Input = ({ label, type = "text", ...props }) => (
  <div className="flex flex-col">
    <label className="text-sm font-medium text-gray-700 mb-1">
      {label}
    </label>
    <input
      type={type}
      className="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
      {...props}
      required
    />
  </div>
);

export default NewAccountPage;
