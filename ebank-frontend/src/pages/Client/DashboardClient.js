import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../auth/AuthContext';
import {
  fetchDashboardData,
  fetchClientAccounts,
  makeTransfer
} from '../../api/ClientService';
import './DashboardClient.css';

const DashboardClient = () => {
  const { isAuthenticated, logoutUser } = useAuth();

  /* ================= STATES ================= */

  const [accountsList, setAccountsList] = useState([]);
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [accountDetails, setAccountDetails] = useState(null);

  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [virementData, setVirementData] = useState({
    sourceRib: '',
    destinationRib: '',
    amount: '',
    reason: ''
  });

  /* ================= LOAD DASHBOARD ================= */

  const loadDashboard = useCallback(async () => {
    if (!isAuthenticated) return;

    setIsLoading(true);
    setError('');

    try {
      const accounts = await fetchClientAccounts();
      setAccountsList(accounts);

      if (!accounts || accounts.length === 0) {
        setError("Aucun compte bancaire trouvé.");
        setAccountDetails(null);
        return;
      }

      const accountId = selectedAccountId || accounts[0].id;
      const data = await fetchDashboardData(accountId, page, 10);

      if (data.message) {
        setError(data.message);
        setAccountDetails(null);
      } else {
        setAccountDetails(data);
        setSelectedAccountId(data.accountId);
        setVirementData(prev => ({
          ...prev,
          sourceRib: data.rib
        }));
      }
    } catch (err) {
      console.error(err);
      if (err.response?.status === 401) {
        logoutUser();
      } else {
        setError("Erreur lors du chargement du tableau de bord.");
      }
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, selectedAccountId, page, logoutUser]);

// Dans DashboardClient.js, modifiez la partie où vous traitez la réponse
useEffect(() => {
    loadDashboard();
}, [loadDashboard]);

  /* ================= HANDLERS ================= */

  const handleAccountChange = (e) => {
    setSelectedAccountId(parseInt(e.target.value));
    setPage(0);
  };

  const handleVirementChange = (e) => {
    const { name, value } = e.target;
    setVirementData(prev => ({ ...prev, [name]: value }));
  };

const handleTransferSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    // Check if we have valid account data
    if (!accountDetails || !accountDetails.rib) {
        setError("Veuillez attendre le chargement complet du compte pour effectuer un virement.");
        return;
    }

    if (!virementData.destinationRib || !virementData.amount || virementData.amount <= 0) {
        setError("Veuillez remplir correctement tous les champs.");
        return;
    }

    try {
        // Make the transfer
        const message = await makeTransfer({
            sourceRib: accountDetails.rib,
            destinationRib: virementData.destinationRib,
            amount: parseFloat(virementData.amount),
            reason: virementData.reason || "Virement effectué"
        });

        // Show success message
        setSuccessMessage(message);

        // Clear the form
        setVirementData(prev => ({
            ...prev,
            destinationRib: '',
            amount: '',
            reason: ''
        }));

        // Force a complete refresh of the dashboard data
        setIsLoading(true);
        try {
            // First, reload the accounts list
            const updatedAccounts = await fetchClientAccounts();
            setAccountsList(updatedAccounts);

            if (updatedAccounts && updatedAccounts.length > 0) {
                // Then reload the dashboard data
                const accountId = selectedAccountId || updatedAccounts[0].id;
                const data = await fetchDashboardData(accountId, page, 10);
                
                if (data.message) {
                    setError(data.message);
                    setAccountDetails(null);
                } else {
                    setAccountDetails(data);
                    setSelectedAccountId(data.accountId);
                }
            }
        } catch (refreshError) {
            console.error("Error refreshing dashboard:", refreshError);
            // Don't show error to user, just log it
        } finally {
            setIsLoading(false);
        }

    } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || err.response?.data || "Échec du virement. Veuillez réessayer.");
    }
};

  /* ================= RENDER ================= */

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h2>Tableau de Bord Client</h2>
        {accountDetails?.clientName && (
          <div className="welcome-message">
            Bienvenue, <strong>{accountDetails.clientName}</strong>
          </div>
        )}
      </div>
      
      {error && <div className="error">{error}</div>}
      {successMessage && <div className="success">{successMessage}</div>}
      {accountDetails && (
        <div className="dashboard-grid">

          {/* ===== ACCOUNT SUMMARY ===== */}
          <div className="card">
            <h2>Compte bancaire</h2>

            <p>RIB</p>
            <strong>{accountDetails.rib}</strong>

            <p style={{ marginTop: 12 }}>Solde actuel</p>
            <div className="balance">
              {accountDetails.balance.toFixed(2)} €
            </div>

            {accountsList.length > 1 && (
              <select
                value={selectedAccountId ?? ''}
                onChange={handleAccountChange}
                style={{ marginTop: 12 }}
              >
                {accountsList.map(acc => (
                  <option key={acc.id} value={acc.id}>
                    {acc.rib}
                  </option>
                ))}
              </select>
            )}
          </div>
          

          {/* ===== TRANSFER ===== */}
          <div className="card">
            <h2>Nouveau virement</h2>

            <form onSubmit={handleTransferSubmit}>
                <input
                type="text"
                name="sourceRib"
                placeholder="RIB émetteur"
                value={accountDetails.rib} // Utiliser accountDetails.rib pour le visuel
                readOnly
                />
              <input
                type="text"
                name="destinationRib"
                placeholder="RIB destinataire"
                value={virementData.destinationRib}
                onChange={handleVirementChange}
                required
              />

              <input
                type="number"
                name="amount"
                placeholder="Montant (€)"
                value={virementData.amount}
                onChange={handleVirementChange}
                min="0.01"
                step="0.01"
                required
              />

              <input
                type="text"
                name="reason"
                placeholder="Motif"
                value={virementData.reason}
                onChange={handleVirementChange}
              />

              <button type="submit">Valider le virement</button>
            </form>
          </div>

          {/* ===== OPERATIONS ===== */}
          <div className="card" style={{ gridColumn: '1 / -1' }}>
            <h2>Historique des opérations</h2>

            <table>
              <thead>
                <tr>
                  <th>Libellé</th>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Montant</th>
                </tr>
              </thead>
              <tbody>
                {accountDetails.operations.map((op, i) => (
                  <tr key={i}>
                    <td>{op.title}</td>
                    <td>{new Date(op.date).toLocaleString()}</td>
                    <td className={op.type === 'DEBIT' ? 'debit' : 'credit'}>
                      {op.type}
                    </td>
                    <td>{op.amount.toFixed(2)} €</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* ===== PAGINATION ===== */}
            <div className="pagination">
              <button
                onClick={() => setPage(p => p - 1)}
                disabled={page === 0}
              >
                Précédent
              </button>

              <span>
                Page {page + 1} / {accountDetails.totalPages}
              </span>

              <button
                onClick={() => setPage(p => p + 1)}
                disabled={page + 1 >= accountDetails.totalPages}
              >
                Suivant
              </button>
            </div>
          </div>

        </div>
      )}
    </div>
  );
};

export default DashboardClient;
